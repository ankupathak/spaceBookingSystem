package com.ls.spaceBookingSystem.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/")
    public ResponseEntity<String> createAccount() {

        return new ResponseEntity<>("All ok", HttpStatus.OK);
    }

    @GetMapping("/aa")
    public ResponseEntity<String> login() {

        return new ResponseEntity<>("All ok", HttpStatus.OK);
    }
}


