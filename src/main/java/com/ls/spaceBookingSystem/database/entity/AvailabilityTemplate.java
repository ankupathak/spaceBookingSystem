package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

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

//    @OneToMany(mappedBy = "id.templateId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<AvailabilityRule> rules = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private List<AvailabilityRule> rules = new ArrayList<>();
}
