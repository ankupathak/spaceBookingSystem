package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.dtos.requests.LoginRequest;
import com.ls.spaceBookingSystem.dtos.requests.VerifyAndLoginRequest;
import com.ls.spaceBookingSystem.dtos.responses.TokenResponse;
import com.ls.spaceBookingSystem.services.AuthService;
import com.ls.spaceBookingSystem.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("create-account")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest data) {
        String emailOtp = authService.registration(data);
        return new ResponseEntity<>(emailOtp, HttpStatus.CREATED);
    }

    @PostMapping("verify-and-login")
    public ResponseEntity<TokenResponse> verifyAndLogin(@Valid @RequestBody VerifyAndLoginRequest data, HttpServletResponse response) {
        return new ResponseEntity<>(
                authService.verifyOtpAndLogin(data, response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = jwtService.extractUserId(
                jwtService.extractTokenFromRequest(request)
        );
        authService.logout(userId, response);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken, response));
    }
}
