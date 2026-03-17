package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.CreateAccountRequest;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {

    @Autowired
    private AuthService authService;

    @GetMapping("test")
    public  ResponseEntity<?> testTest() {
        return new ResponseEntity<>("all good test Test--->", HttpStatus.OK);
    }
}
