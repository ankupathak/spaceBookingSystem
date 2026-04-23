package com.ls.spaceBookingSystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "spaces")
public class Space extends BaseTimeStamp {
    @Id
    @Column(name = "space_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spaceId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String description;

    @Column(name = "is_active")
    private boolean isActive;
}
