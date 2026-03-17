package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.constants.OtpTypes;
import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.dtos.requests.LoginRequest;
import com.ls.spaceBookingSystem.dtos.requests.VerifyAndLoginRequest;
import com.ls.spaceBookingSystem.dtos.responses.TokenResponse;
import com.ls.spaceBookingSystem.entity.RefreshToken;
import com.ls.spaceBookingSystem.entity.User;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.RefreshTokenRepository;
import com.ls.spaceBookingSystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private  OtpService otpService;

    @Autowired
    private  JwtService jwtService;

    @Autowired
    private  CookieService cookieService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private  EmailTemplateService emailTemplateService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public String registration(CreateAccountRequest user) {

        User newUser = userRepository.findByEmail(user.getEmail())
                .orElseGet(() -> createNewUser(user));

        if(newUser.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_TAKEN);
        }

        return otpService.getEmailOtp(newUser, OtpTypes.REGISTRATION_EMAIL_OTP);

    }

    @Transactional
    public TokenResponse verifyOtpAndLogin(VerifyAndLoginRequest data, HttpServletResponse response) {

        User user = userRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        OtpTypes otpTypes = OtpTypes.REGISTRATION_EMAIL_OTP;
        otpService.verifyOtp(user.getUserId(), otpTypes.getId(), data.getOtp());

        user.setEmailVerified(true);
        userRepository.save(user);

        String html = emailTemplateService.renderNewUserWelcomeEmail(user.getFullName());
        emailService.sendMail(user.getEmail(),"\uD83D\uDC4B Welcome to Space Booking", html);

        return issueTokens(user, response);
    }

    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        return issueTokens(user, response);
    }

    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        refreshTokenRepository.deleteByUserUserId(userId);
        userRepository.incrementTokenVersion(userId);
        cookieService.clearRefreshCookie(response);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken, HttpServletResponse response) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenHash = hashToken(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User user        = storedToken.getUser();
        int  dbVersion   = user.getTokenVersion();
        int  tokenVersion = jwtService.extractVersion(refreshToken);

        if (tokenVersion != dbVersion) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newJti         = jwtService.extractJti(newAccessToken);

        storedToken.setActiveJti(newJti);

        if (jwtService.shouldRotate(refreshToken)) {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            storedToken.setTokenHash(hashToken(newRefreshToken));
            storedToken.setExpiresAt(LocalDateTime.now().plusDays(30));
            cookieService.setRefreshCookie(response, newRefreshToken);
        }

        int updated = refreshTokenRepository.updateIfUnchanged(
                storedToken.getId(),
                newJti,
                storedToken.getTokenHash(),
                storedToken.getExpiresAt(),
                storedToken.getUpdatedAt()
        );

        if (updated == 0) {
            // Someone else updated it between our read and write
            throw new AppException(ErrorCode.CONCURRENT_REFRESH);
        }

        return new TokenResponse(newAccessToken);
    }

    private User createNewUser(CreateAccountRequest user) {
        try {
            User newUser = new User();
            newUser.setFullName(user.getFullName());
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            newUser.setEmail(user.getEmail());
            return userRepository.save(newUser);
        } catch(Exception e) {
            throw new AppException(ErrorCode.UNEXPECTED).withDevMessage(String.valueOf(e));
        }
    }

    @Transactional
    private TokenResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String jti          = jwtService.extractJti(accessToken);
        log.error("jti={}",jti);
        // Save new refresh token
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUserId(user.getUserId());
        refreshTokenEntity.setTokenHash(hashToken(refreshToken));
        refreshTokenEntity.setActiveJti(jti);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(30));
        refreshTokenRepository.save(refreshTokenEntity);

        cookieService.setRefreshCookie(response, refreshToken);
        return new TokenResponse(accessToken);
    }

    private String hashToken(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
