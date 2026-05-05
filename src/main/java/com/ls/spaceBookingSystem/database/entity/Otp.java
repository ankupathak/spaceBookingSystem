package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Data
@Table(name = "otps")
@EqualsAndHashCode(callSuper = true)
public class Otp extends BaseTimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_type_id", nullable = false)
    private int otpTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "otp_type_id",
            insertable = false,
            updatable = false
    )
    private OtpType otpType;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "remaining_attempts")
    private short remainingAttempts = 3;

    @Column(name = "expired_at")
    private Instant expiredAt;
}
