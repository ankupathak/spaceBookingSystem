package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.BookingRequestDto;
import com.ls.spaceBookingSystem.dtos.responses.BookingResponseDto;
import com.ls.spaceBookingSystem.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getMyBooking(bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> create(
            @Valid @RequestBody BookingRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.validateAndCreateBooking(request));
    }
}
