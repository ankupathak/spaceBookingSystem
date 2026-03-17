package com.ls.spaceBookingSystem.SpringSecurity;

import com.ls.spaceBookingSystem.repository.UserRepository;
import com.ls.spaceBookingSystem.services.CustomUserDetailsService;
import com.ls.spaceBookingSystem.services.JwtService;
import com.ls.spaceBookingSystem.services.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenBlacklistService blacklistService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = jwtService.extractTokenFromRequest(request);

        if (token != null && jwtService.isTokenValid(token)) {

            Claims claims = jwtService.extractClaims(token);
            Long userId = Long.parseLong(claims.getSubject());

            // 1. Check emergency user-level revocation (Redis)
            if (blacklistService.isUserEmergencyRevoked(userId, claims.getIssuedAt())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Account access revoked");
                return;
            }

            // 2. Check jti blacklist (Redis — for compromised tokens)
            String jti = claims.getId();
            if (jti != null && blacklistService.isJtiBlacklisted(jti)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been revoked");
                return;
            }

            // 3. Set authentication
            UserDetails userDetails = userDetailsService
                    .loadUserByUsername(claims.get("email", String.class));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
