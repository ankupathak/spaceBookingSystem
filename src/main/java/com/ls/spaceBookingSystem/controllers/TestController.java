package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.messageBroker.rabbitMq.Producers.EmailProducer;
import com.ls.spaceBookingSystem.services.email.BookingConfirmEmailTemplate;
import com.ls.spaceBookingSystem.services.email.WelcomeEmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private EmailProducer emailProducer;

    @GetMapping("produce_events")
    public ResponseEntity<String> produceEvent() {
        Random r= new Random();
        LocalDateTime now = LocalDateTime.now();
//        WelcomeEmailTemplate template = new WelcomeEmailTemplate("ankupathak.work.test@gmail.com", "Mr.Robot"+r.nextInt(1000));
        BookingConfirmEmailTemplate template = new BookingConfirmEmailTemplate(
                "ankupathak.work.test@gmail.com",
                "Mr.Robot"+r.nextInt(1000),
                "Test Space",
                now,
                "Asia/Kolkata",
                "BKG-00123"
        );

        emailProducer.publishEmailEvent(template);
        return ResponseEntity.ok("Event published");
    }
}
