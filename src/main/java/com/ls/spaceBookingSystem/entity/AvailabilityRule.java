package com.ls.spaceBookingSystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
@Table(name = "availability_rules")
public class AvailabilityRule extends BaseTimeStamp {
    @EmbeddedId
    private AvailabilityRulePrimaryKey id;

    @Column(columnDefinition = "json")
    private String slots;
}
