package com.ls.spaceBookingSystem.SpringSecurity;

import com.ls.spaceBookingSystem.config.JwtProperties;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.UserRepository;
import com.ls.spaceBookingSystem.services.CustomUserDetailsService;
import com.ls.spaceBookingSystem.services.JwtService;
import com.ls.spaceBookingSystem.services.TokenBlacklistService;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    JwtProperties jwtProperties;

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

        String token = jwtService.extractAuthTokenFromRequest(request);


        if (token != null) {
            AccessTokenData accessTokenData = jwtService.validateAndExtract(token, jwtProperties.getAccessType());

            if (blacklistService.isTokenBlackListedOrInvalidated(accessTokenData.getDeviceId(), accessTokenData.getUserId(), accessTokenData.getIssuedAt())) {
                throw new AppException(ErrorCode.TOKEN_REVOKED);
            }

            List<GrantedAuthority> authorities = accessTokenData.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            accessTokenData, null, authorities);

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
