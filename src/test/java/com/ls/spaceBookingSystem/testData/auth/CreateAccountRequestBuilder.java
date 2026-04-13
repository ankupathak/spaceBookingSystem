package com.ls.spaceBookingSystem.testData.auth;

import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;

public class CreateAccountRequestBuilder {
    private String email = "test@mail.com";
    private String password = "password";
    private String fullName = "Test User";

    public CreateAccountRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CreateAccountRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public CreateAccountRequest build() {
        return new CreateAccountRequest(fullName, email, password);
    }
}
