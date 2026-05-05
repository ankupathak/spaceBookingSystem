package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "spaces")
public class Space extends BaseTimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private AvailabilityTemplate template;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
