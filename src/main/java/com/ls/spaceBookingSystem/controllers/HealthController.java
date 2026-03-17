package com.ls.spaceBookingSystem.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("health-check")
@RestController
public class HealthController {

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("health-check: All ok", HttpStatus.OK);
    }
}


