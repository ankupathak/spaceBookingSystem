package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.config.JwtProperties;
import com.ls.spaceBookingSystem.constants.OtpTypes;
import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.dtos.requests.LoginRequest;
import com.ls.spaceBookingSystem.dtos.requests.VerifyAndLoginRequest;
import com.ls.spaceBookingSystem.dtos.responses.TokenResponse;
import com.ls.spaceBookingSystem.entity.AuthDevice;
import com.ls.spaceBookingSystem.entity.AuthDeviceId;
import com.ls.spaceBookingSystem.entity.User;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.AuthDeviceRepository;
import com.ls.spaceBookingSystem.repository.UserRepository;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import com.ls.spaceBookingSystem.services.jwt.data.RefreshTokenData;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthDeviceRepository authDeviceRepository;

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
    private TokenBlacklistService blacklistService;

    @Autowired
    private JwtProperties jwtProperties;

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
    public void logout(String refreshToken, HttpServletResponse response) {

        if(refreshToken.isEmpty()) {
            return;
        }

        RefreshTokenData refreshTokenData = jwtService.validateAndExtract(refreshToken, jwtProperties.getRefreshType());

        String key = refreshTokenData.getUserId()+":"+refreshTokenData.getDeviceId();
        if(blacklistService.containsKey(key)) {
            cookieService.clearRefreshCookie(response);
            return;
        }


        AuthDevice authDeviceEntity = authDeviceRepository.findByIdDeviceIdAndIdUserId(
                refreshTokenData.getDeviceId(), refreshTokenData.getUserId()
        ).orElse(null);


        if(authDeviceEntity == null) {
            cookieService.clearRefreshCookie(response);
            return;
        }

        Instant currentTime = Instant.now();
        if(authDeviceEntity.getExpiresAt().isAfter(currentTime)) {
            authDeviceEntity.setExpiresAt(currentTime);
            authDeviceRepository.save(authDeviceEntity);
        } else {
            currentTime = authDeviceEntity.getExpiresAt();
        }

        long minsUntilExpiry = Duration.between(currentTime, Instant.now()).toMinutes();
        blacklistService.blacklist(key,"1", minsUntilExpiry);

        cookieService.clearRefreshCookie(response);

    }

    @Transactional
    public TokenResponse refresh(String refreshToken, HttpServletRequest request, HttpServletResponse response) {

        if(refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }


        RefreshTokenData refreshTokenData = jwtService.validateAndExtract(refreshToken, jwtProperties.getRefreshType());

        AuthDevice authDeviceEntity = authDeviceRepository.findByIdDeviceIdAndIdUserId(refreshTokenData.getDeviceId(),refreshTokenData.getUserId())
                .orElseThrow(() -> {
                    cookieService.clearRefreshCookie(response);
                    return new AppException(ErrorCode.UNAUTHENTICATED);
                });

        Instant tokenExpiry   = authDeviceEntity.getExpiresAt();
        if (tokenExpiry.isBefore(Instant.now())) {
            cookieService.clearRefreshCookie(response);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = authDeviceEntity.getUser();
        Instant  tokenValidAfter   = user.getTokenValidAfter();

        if (!tokenValidAfter.equals(refreshTokenData.getValidAfter())) {
            cookieService.clearRefreshCookie(response);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        AccessTokenData accessTokenData = new AccessTokenData();
        accessTokenData.setUserId(user.getUserId());
        accessTokenData.setDeviceId(authDeviceEntity.getId().getDeviceId());
        List<String> roles = user.getRoles().stream()
                .map(ur -> ur.getRole().getRoleName())
                .toList();
        accessTokenData.setRoles(roles);
        String newAccessToken  = jwtService.generate(jwtProperties.getAccessType(), accessTokenData);


        if (jwtService.shouldRotate(refreshTokenData)) {
            RefreshTokenData newRefreshTokenData = new RefreshTokenData();
            refreshTokenData.setUserId(user.getUserId());
            refreshTokenData.setDeviceId(authDeviceEntity.getId().getDeviceId());
            refreshTokenData.setValidAfter(user.getTokenValidAfter());
            String newRefreshToken = jwtService.generate(jwtProperties.getRefreshType(),refreshTokenData);

            cookieService.setRefreshCookie(response, newRefreshToken);
        }

        int updated = authDeviceRepository.updateIfUnchanged(
                authDeviceEntity.getId().getDeviceId(),
                authDeviceEntity.getId().getUserId(),
                authDeviceEntity.getExpiresAt().plus(jwtProperties.getAccessExpiryInDays(), ChronoUnit.DAYS)
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

        RefreshTokenData refreshTokenData = new RefreshTokenData();
        refreshTokenData.setUserId(user.getUserId());
        refreshTokenData.setDeviceId(UUID.randomUUID().toString());
        refreshTokenData.setValidAfter(user.getTokenValidAfter());
        String refreshToken = jwtService.generate(jwtProperties.getRefreshType(),refreshTokenData);

        AccessTokenData accessTokenData = new AccessTokenData();
        accessTokenData.setUserId(user.getUserId());
        accessTokenData.setDeviceId(refreshTokenData.getDeviceId());
        List<String> roles = user.getRoles().stream()
                .map(ur -> ur.getRole().getRoleName())
                .toList();
        accessTokenData.setRoles(roles);
        String accessToken = jwtService.generate(jwtProperties.getAccessType(), accessTokenData);

        AuthDevice authDeviceEntity = new AuthDevice();
        AuthDeviceId authDeviceId = new AuthDeviceId();
        authDeviceId.setDeviceId(refreshTokenData.getDeviceId());
        authDeviceId.setUserId(refreshTokenData.getUserId());
        authDeviceEntity.setId(authDeviceId);
        authDeviceEntity.setUser(user);
        authDeviceRepository.save(authDeviceEntity);

        cookieService.setRefreshCookie(response, refreshToken);
        return new TokenResponse(accessToken);
    }

    private String hashToken(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
