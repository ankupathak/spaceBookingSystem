package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.config.JwtProperties;
import com.ls.spaceBookingSystem.constants.OtpTypes;
import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.dtos.requests.VerifyAndLoginRequest;
import com.ls.spaceBookingSystem.dtos.responses.TokenResponse;
import com.ls.spaceBookingSystem.entity.*;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.AuthDeviceRepository;
import com.ls.spaceBookingSystem.repository.OtpRepository;
import com.ls.spaceBookingSystem.repository.UserRepository;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import com.ls.spaceBookingSystem.services.jwt.data.RefreshTokenData;
import com.ls.spaceBookingSystem.testData.auth.CreateAccountRequestBuilder;
import com.ls.spaceBookingSystem.testData.auth.VerifyAndLoginRequestBuilder;
import com.ls.spaceBookingSystem.testData.shared.UserBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks AuthService authService;
    @Mock UserRepository userRepository;
    @Mock OtpRepository otpRepository;
    @Mock OtpService otpService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailTemplateService emailTemplateService;
    @Mock EmailService emailService;
    @Mock JwtProperties jwtProperties;
    @Mock JwtService jwtService;
    @Mock AuthDeviceRepository authDeviceRepository;
    @Mock CookieService cookieService;
    @Mock HttpServletRequest httpServletRequest;
    @Mock HttpServletResponse httpServletResponse;
    @Mock TokenBlacklistService tokenBlacklistService;


//:::::::::::::::::::::::::: registration :::::::::::::::::::::::::::::::::::::::::::::::::
    @Nested
    class RegistrationTests {

        @Test
        void shouldCreateUserAndSendOtp() {
            CreateAccountRequest request = new CreateAccountRequestBuilder().build();
            User savedUser = new UserBuilder()
                    .withEmail(request.getEmail())
                    .withPassword(request.getPassword())
                    .build();

            when(passwordEncoder.encode(anyString())).thenReturn(savedUser.getPassword());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(otpService.getEmailOtp(any(User.class), any(OtpTypes.class))).thenReturn("Otp is sent");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            String result = authService.registration(request);
            assertThat(result).contains("Otp is sent");
        }

        @Test
        void shouldSendOtp_whenUserExistsButNotVerified() {
            CreateAccountRequest request = new CreateAccountRequestBuilder().build();
            User savedUser = new UserBuilder()
                    .withEmail(request.getEmail())
                    .withPassword(request.getPassword())
                    .build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(savedUser));
            when(otpService.getEmailOtp(any(User.class), any(OtpTypes.class))).thenReturn("Otp is sent");

            String result = authService.registration(request);
            assertThat(result).isEqualTo("Otp is sent");
        }

        @Test
        void shouldThrow_whenEmailAlreadyVerified() {
            CreateAccountRequest request = new CreateAccountRequestBuilder().build();
            User savedUser = new UserBuilder()
                    .withEmail(request.getEmail())
                    .withPassword(request.getPassword())
                    .verified()
                    .build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(savedUser));

            assertThatThrownBy(() -> authService.registration(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.EMAIL_TAKEN);
        }

        static Stream<Arguments> otpFailureScenerio() {
            return Stream.of(
                    Arguments.of("OTP_MAX_ATTEMPTS", ErrorCode.OTP_MAX_ATTEMPTS),
                    Arguments.of("OTP_RESEND_TOO_SOON", ErrorCode.OTP_RESEND_TOO_SOON)
            );
        }
        @ParameterizedTest
        @MethodSource("otpFailureScenerio")
        void shouldPropagateOtpException(String scenario, ErrorCode expected) {
            CreateAccountRequest request = new CreateAccountRequestBuilder().build();
            User savedUser = new UserBuilder()
                    .withEmail(request.getEmail())
                    .withPassword(request.getPassword())
                    .build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(savedUser));
            when(otpService.getEmailOtp(any(User.class),any(OtpTypes.class))).thenThrow(new AppException(expected));

            assertThatThrownBy(() -> authService.registration(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(expected);
        }
    }

    @Nested
    class VerifyOtpAndLoginTests {
        @Test
        void shouldVerifyOtp_andLoginSuccessfully() {
            VerifyAndLoginRequest request = new VerifyAndLoginRequestBuilder().build();
            User user = new UserBuilder()
                    .withEmail(request.getEmail())
                    .build();

            String accessToken = "access-token";
            String refreshToken = "refresh-token";
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            doNothing().when(otpService).verifyOtp(anyLong(),anyInt(),anyString());
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(emailTemplateService.renderNewUserWelcomeEmail(anyString())).thenReturn("welcome html");
            doNothing().when(emailService).sendMail(anyString(),anyString(),anyString());

            when(jwtProperties.getAccessType()).thenReturn("access");
            when(jwtProperties.getRefreshType()).thenReturn("refresh");

            when(jwtService.generate(eq("refresh"),any(RefreshTokenData.class))).thenReturn(refreshToken);
            when(jwtService.generate(eq("access"),any(AccessTokenData.class))).thenReturn(accessToken);

            when(authDeviceRepository.save(any(AuthDevice.class))).thenReturn(new AuthDevice());
            doNothing().when(cookieService).setRefreshCookie(any(HttpServletResponse.class),anyString());


//            AuthService spyAuthService = Mockito.spy(authService);
            TokenResponse result = authService.verifyOtpAndLogin(request,httpServletResponse);

            assertThat(result.getAccessToken()).isEqualTo(accessToken);
            assertThat(user.isEmailVerified()).isTrue();

            verify(userRepository).findByEmail(request.getEmail());
            verify(otpService).verifyOtp(user.getUserId(), OtpTypes.REGISTRATION_EMAIL_OTP.getId(), request.getOtp());
            verify(userRepository).save(user);
            verify(emailTemplateService).renderNewUserWelcomeEmail(user.getFullName());
            verify(emailService).sendMail(eq(user.getEmail()), anyString(), anyString());
            verify(authDeviceRepository).save(any(AuthDevice.class));
            verify(cookieService).setRefreshCookie(httpServletResponse, refreshToken);
        }

        @Test
        void shouldThrow_whenUserNotFound() {
            VerifyAndLoginRequest request = new VerifyAndLoginRequestBuilder()
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(request, httpServletResponse))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_FOUND);

            verify(userRepository).findByEmail(request.getEmail());
            verifyNoMoreInteractions(otpService, emailService, emailTemplateService, jwtService, authDeviceRepository, cookieService);
        }

        @Test
        void shouldPropagateOtpFailure() {
            VerifyAndLoginRequest request = new VerifyAndLoginRequestBuilder()
                    .withOtp("2314")
                    .build();

            User user = new UserBuilder()
                    .withEmail(request.getEmail())
                    .verified()
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            doThrow(new AppException(ErrorCode.OTP_INVALID))
                    .when(otpService).verifyOtp(user.getUserId(), OtpTypes.REGISTRATION_EMAIL_OTP.getId(), request.getOtp());

            assertThatThrownBy(() -> authService.verifyOtpAndLogin(request, httpServletResponse))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.OTP_INVALID);

            verify(userRepository).findByEmail(request.getEmail());
            verify(otpService).verifyOtp(user.getUserId(), OtpTypes.REGISTRATION_EMAIL_OTP.getId(), request.getOtp());
            verifyNoMoreInteractions(userRepository, emailService, emailTemplateService, jwtService, authDeviceRepository, cookieService);
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void shouldReturn_whenRefreshTokenEmpty() {
            authService.logout("", httpServletResponse);

            verifyNoInteractions(jwtService, authDeviceRepository, tokenBlacklistService);
            verifyNoInteractions(cookieService);
        }

        @Test
        void shouldClearCookie_whenTokenAlreadyBlacklisted() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);

            String key = "1:device";
            when(tokenBlacklistService.containsKey(key)).thenReturn(true);

            authService.logout(refreshToken, httpServletResponse);

            verify(cookieService).clearRefreshCookie(httpServletResponse);
            verifyNoMoreInteractions(authDeviceRepository);
        }

        @Test
        void shouldClearCookie_whenDeviceNotFound() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);

            String key = "1:device";
            when(tokenBlacklistService.containsKey(key)).thenReturn(false);

            when(authDeviceRepository.findByIdDeviceIdAndIdUserId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            authService.logout(refreshToken, httpServletResponse);
            verify(cookieService).clearRefreshCookie(httpServletResponse);
        }

        @Test
        void shouldExpireDeviceAndBlacklist_whenDeviceActive() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");

            AuthDevice device = new AuthDevice();
            device.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);

            String key = "1:device";
            when(tokenBlacklistService.containsKey(key)).thenReturn(false);

            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            when(authDeviceRepository.save(any())).thenReturn(device);

            authService.logout(refreshToken, httpServletResponse);

            verify(authDeviceRepository).save(device);
            verify(tokenBlacklistService).blacklist(eq(key), eq("1"), anyLong());
            verify(cookieService).clearRefreshCookie(httpServletResponse);
        }

        @Test
        void shouldBlacklistWithoutUpdate_whenDeviceAlreadyExpired() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");

            AuthDevice device = new AuthDevice();
            device.setExpiresAt(Instant.now().minusSeconds(300)); // past → expired

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);

            String key = "1:device";
            when(tokenBlacklistService.containsKey(key)).thenReturn(false);

            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            authService.logout(refreshToken, httpServletResponse);
            verify(authDeviceRepository, never()).save(any());
            verify(tokenBlacklistService).blacklist(eq(key), eq("1"), anyLong());
            verify(cookieService).clearRefreshCookie(httpServletResponse);
        }
    }

    @Nested
    class RefreshTests {
        @Test
        void shouldRefreshSuccessfully_withoutRotation() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");
            tokenData.setValidAfter(Instant.now());

            User user = new UserBuilder()
                    .withId(1L)
                    .withTokenValidAfter(tokenData.getValidAfter())
                    .build();

            AuthDevice device = new AuthDevice();
            device.setUser(user);
            device.setExpiresAt(Instant.now().plusSeconds(300));
            device.setId(new AuthDeviceId(1L,"device"));

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtProperties.getAccessType()).thenReturn("access");

            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);
            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            when(jwtService.generate(eq("access"), any())).thenReturn("new-access");
            when(jwtService.shouldRotate(tokenData)).thenReturn(false);

            TokenResponse responseObj = authService.refresh(refreshToken, httpServletRequest, httpServletResponse);

            assertThat(responseObj.getAccessToken()).isEqualTo("new-access");
            verify(authDeviceRepository,never()).updateIfUnchanged(any(), any(), any());
        }

        @Test
        void shouldRefreshSuccessfully_withRotation() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");
            tokenData.setValidAfter(Instant.now());

            User user = new UserBuilder()
                    .withId(1L)
                    .withTokenValidAfter(tokenData.getValidAfter())
                    .build();

            AuthDevice device = new AuthDevice();
            device.setUser(user);
            device.setExpiresAt(Instant.now().plusSeconds(300));
            device.setId(new AuthDeviceId(1L,"device"));

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtProperties.getAccessType()).thenReturn("access");
            when(jwtProperties.getRefreshExpiryInDays()).thenReturn(30);

            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);
            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            when(jwtService.generate(eq("access"), any())).thenReturn("new-access");
            when(jwtService.shouldRotate(tokenData)).thenReturn(true);
            when(authDeviceRepository.updateIfUnchanged(anyString(), anyLong(), any(Instant.class))).thenReturn(1);

            TokenResponse responseObj = authService.refresh(refreshToken, httpServletRequest, httpServletResponse);

            assertThat(responseObj.getAccessToken()).isEqualTo("new-access");
            verify(authDeviceRepository).updateIfUnchanged(anyString(), anyLong(), any(Instant.class));
        }

        @Test
        void shouldClearCookie_whenTokenExpired() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");

            AuthDevice device = new AuthDevice();
            device.setExpiresAt(Instant.now().minusSeconds(60));

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);
            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            assertThatThrownBy(() -> authService.refresh(refreshToken, httpServletRequest, httpServletResponse))
                    .isInstanceOf(AppException.class);

            verify(cookieService).clearRefreshCookie(httpServletResponse);
        }

        @Test
        void shouldClearCookie_whenTokenValidAfterMismatch() {
            String refreshToken = "token";

            RefreshTokenData tokenData = new RefreshTokenData();
            tokenData.setUserId(1L);
            tokenData.setDeviceId("device");
            tokenData.setValidAfter(Instant.now().minusSeconds(100));

            User user = new UserBuilder()
                    .withId(1L)
                    .withTokenValidAfter(Instant.now()) // mismatch
                    .build();

            AuthDevice device = new AuthDevice();
            device.setExpiresAt(Instant.now().plusSeconds(300));
            device.setUser(user);

            when(jwtProperties.getRefreshType()).thenReturn("refresh");
            when(jwtService.validateAndExtract(refreshToken, "refresh")).thenReturn(tokenData);
            when(authDeviceRepository.findByIdDeviceIdAndIdUserId("device", 1L))
                    .thenReturn(Optional.of(device));

            assertThatThrownBy(() -> authService.refresh(refreshToken, httpServletRequest, httpServletResponse))
                    .isInstanceOf(AppException.class);

            verify(cookieService).clearRefreshCookie(httpServletResponse);
        }
    }
}
