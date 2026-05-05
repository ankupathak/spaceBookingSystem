package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.common.enums.BookingStatus;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import com.ls.spaceBookingSystem.common.utils.BitmaskUtil;
import com.ls.spaceBookingSystem.common.validations.validators.BookingValidator;
import com.ls.spaceBookingSystem.database.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.database.entity.Booking;
import com.ls.spaceBookingSystem.database.entity.Space;
import com.ls.spaceBookingSystem.database.repository.BookingRepository;
import com.ls.spaceBookingSystem.database.repository.SpaceRepository;
import com.ls.spaceBookingSystem.dtos.requests.BookingRequestDto;
import com.ls.spaceBookingSystem.dtos.responses.BookingResponseDto;
import com.ls.spaceBookingSystem.mapper.BookingMapper;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SpaceRepository spaceRepository;
    private final AuthService authService;
    private final BookingValidator bookingValidator;
    private final TimezoneService timezoneService;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    @Transactional(rollbackFor = Exception.class)
    public BookingResponseDto validateAndCreateBooking(BookingRequestDto req) {

        AccessTokenData auth = authService.getLoggedInUserData();

        // Step 1: Fetching Booker time zone and converting requested slots to Instant
        String bookerTimezoneStr = userService.getUserTimezone(auth.getUserId());
        ZoneId bookerZoneId = timezoneService.parseAndValidate(bookerTimezoneStr);
        Instant start = timezoneService.toInstant(timezoneService.truncateToMinute(req.requestedStart()), bookerZoneId);
        Instant end   = timezoneService.toInstant(timezoneService.truncateToMinute(req.requestedEnd()), bookerZoneId);

        // Step 2: Fetch the space
        Space space = spaceRepository.findActiveWithTemplateAndRules(req.spaceId())
                .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        AvailabilityTemplate template = space.getTemplate();

        // Step 3: Fetching Space owner Timezone
        String ownerTimezoneStr = userService.getUserTimezone(space.getUserId());
        ZoneId ownerZoneId = timezoneService.parseAndValidate(ownerTimezoneStr);

        // Step 4: Validations
        long durationMinutes = ChronoUnit.MINUTES.between(start, end);
        bookingValidator.validateBookingRequest(
                template,
                ownerZoneId,
                start,
                end,
                bookerZoneId,
                durationMinutes
        );

        // Step 5: Build the Booking object
        int buffer = template.getBufferMinutes();
        Booking booking = Booking.builder()
                .spaceId(req.spaceId())
                .bookerUserId(auth.getUserId())
                .start(start)
                .end(end)
                .bookerTimezone(bookerTimezoneStr)
                .status(BookingStatus.CONFIRMED) // We skip PENDING and go straight to CONFIRMED!
                .bufferMinutes(buffer)
                .templateVersion(template.getTemplateVersion())
                .ruleVersion(template.getRulesVersion())
                .build();

        // Step 6: The "Dumb" Insert
        try {
            // We blindly insert. If there is a collision, the PostgreSQL GiST index
            // acts as a physical shield and throws an integrity exception.
            Booking saved = bookingRepository.save(booking);
            return bookingMapper.toResponse(saved);

        } catch (DataIntegrityViolationException e) {
            System.out.println(e);
            throw new AppException(ErrorCode.SPACE_NOT_AVAILABILE, "This time slot is already taken. Please choose another time.")
                    .withDevMessage(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getMyBooking(Long bookingId) {
        AccessTokenData auth = authService.getLoggedInUserData();

        Booking booking = bookingRepository.findConfirmedById(bookingId, auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings() {
        AccessTokenData auth = authService.getLoggedInUserData();
        return bookingRepository
                .findByBookerUserIdAndStatusOrderByStartAsc(auth.getUserId(), BookingStatus.CONFIRMED)
                .stream().map(bookingMapper::toResponse).toList();
    }
}
