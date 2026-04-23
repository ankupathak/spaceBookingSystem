package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.CreateSpaceRequest;
import com.ls.spaceBookingSystem.services.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("space")
public class SpaceController {

//    @Autowired
//    SpaceService spaceService;

//    @PostMapping
//    public ResponseEntity<String> createSpace(@RequestBody CreateSpaceRequest data) {
////        spaceService.createSpace(data);
//        return ResponseEntity.ok("Space Created");
//    }
}
