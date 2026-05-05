package com.ls.spaceBookingSystem.mapper;

import com.ls.spaceBookingSystem.database.entity.Booking;
import com.ls.spaceBookingSystem.database.entity.Space;
import com.ls.spaceBookingSystem.dtos.responses.BookingResponseDto;
import com.ls.spaceBookingSystem.services.TimezoneService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class BookingMapper {
    @Autowired
    private TimezoneService timezoneService;

    /**
     * Maps a single Booking → BookingResponse.
     *
     * space field: MapStruct automatically calls toSpaceDto(Space)
     * for the nested object since the types match.
     *
     * All Instant fields use @Named methods that read bookerTimezone
     * from the booking itself — no extra parameter needed.
     */
    @Mapping(target = "space", source = "space")
    @Mapping(target = "requestedStart", source = "booking", qualifiedByName = "mapStart")
    @Mapping(target = "requestedEnd",   source = "booking", qualifiedByName = "mapEnd")
    @Mapping(target = "createdAt",      source = "booking", qualifiedByName = "mapCreatedAt")
    @Mapping(target = "cancelledAt",    source = "booking", qualifiedByName = "mapCancelledAt")
    public abstract BookingResponseDto toResponse(Booking booking);

    /**
     * Maps a list of Booking entities → list of BookingResponse DTOs.
     * MapStruct generates this automatically from toResponse().
     * Used in getMyBookings() and getSpaceBookings().
     */
    public abstract List<BookingResponseDto> toResponseList(List<Booking> bookings);

    /**
     * Maps Space entity → nested SpaceDto.
     * Called automatically by MapStruct when mapping the space field.
     */
    @Mapping(target = "name",        source = "name")
    @Mapping(target = "description", source = "description")
    public abstract BookingResponseDto.SpaceDto toSpaceDto(Space space);

    // ── Named converters — Instant → ZonedDateTime ────────────────────────
    // Each reads bookerTimezone from the booking itself.
    // Separate named methods needed because all Instant fields have the same
    // type — MapStruct cannot distinguish them without explicit qualification.

    @Named("mapStart")
    protected ZonedDateTime mapStart(Booking booking) {
        return timezoneService.toUserTime(booking.getStart(), booking.getBookerTimezone());
    }

    @Named("mapEnd")
    protected ZonedDateTime mapRequestedEnd(Booking booking) {
        return timezoneService.toUserTime(booking.getEnd(), booking.getBookerTimezone());
    }

    @Named("mapCreatedAt")
    protected ZonedDateTime mapCreatedAt(Booking booking) {
        return timezoneService.toUserTime(booking.getCreatedAt(), booking.getBookerTimezone());
    }

    @Named("mapCancelledAt")
    protected ZonedDateTime mapCancelledAt(Booking booking) {
        if(booking.getCancelledAt() == null) return null;
        return timezoneService.toUserTime(booking.getCancelledAt(), booking.getBookerTimezone());
    }
}
