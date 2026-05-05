package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "auth_devices")
public class AuthDevice extends BaseTimeStamp {

    @EmbeddedId
    private AuthDeviceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
