package com.ls.spaceBookingSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OptimisticLock;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "availability_templates")
public class AvailabilityTemplate extends BaseTimeStamp {

    @Id
    @Column(name = "template_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(name = "user_id")
    private Long userId;

    private String name;

    @Column(name = "min_booking_minutes", nullable = false)
    private int minBookingMinutes;

    @Column(name = "max_booking_minutes", nullable = false)
    private int maxBookingMinutes;

    @Column(name = "buffer_minutes", nullable = false)
    private int bufferMinutes;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Version
    @Column(name = "template_version", nullable = false)
    private int templateVersion;

    @Column(name = "rule_version", nullable = false)
    private int rulesVersion = 1;
}
