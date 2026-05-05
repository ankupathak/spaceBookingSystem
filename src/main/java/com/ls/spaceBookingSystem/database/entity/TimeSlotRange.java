package com.ls.spaceBookingSystem.database.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimeSlotRange(
        @JsonProperty("start") String start,  // "HH:mm" in space owner's timezone
        @JsonProperty("end")   String end
) {}
