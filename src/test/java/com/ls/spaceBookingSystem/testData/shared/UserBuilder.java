package com.ls.spaceBookingSystem.testData.shared;

import com.ls.spaceBookingSystem.entity.User;

import java.time.Instant;

public class UserBuilder {
    private Long userId = 1L;
    private String fullName = "Test User";
    private String email = "test@gmail.com";
    private String password = "password";
    private boolean emailVerified = false;
    private Instant tokenValidAfter = Instant.now();

    public UserBuilder withId(Long userId) {
        this.userId = userId;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder verified() {
        this.emailVerified = true;
        return this;
    }

    public UserBuilder unverified() {
        this.emailVerified = false;
        return this;
    }

    public UserBuilder withTokenValidAfter(Instant tokenValidAfter) {
        this.tokenValidAfter = tokenValidAfter;
        return this;
    }

    public User build() {
        User user = new User();
        user.setFullName(fullName);
        user.setUserId(userId);
        user.setEmail(email);
        user.setPassword(password);
        user.setEmailVerified(emailVerified);
        user.setTokenValidAfter(tokenValidAfter);
        return user;
    }
}
