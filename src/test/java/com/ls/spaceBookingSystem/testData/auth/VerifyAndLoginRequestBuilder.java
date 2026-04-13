package com.ls.spaceBookingSystem.testData.auth;

import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.dtos.requests.VerifyAndLoginRequest;

public class VerifyAndLoginRequestBuilder {
    private String email = "test@mail.com";
    private String otp = "1234";

    public VerifyAndLoginRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public VerifyAndLoginRequestBuilder withOtp(String otp) {
        this.otp = otp;
        return this;
    }

    public VerifyAndLoginRequest build() {
        return new VerifyAndLoginRequest(otp, email);
    }
}
