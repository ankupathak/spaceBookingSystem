package com.ls.spaceBookingSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.type.descriptor.jdbc.TinyIntJdbcType;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "otps")
@EqualsAndHashCode(callSuper = true)
public class Otp extends BaseTimeStamp {

    @Id
    @Column(name = "otp_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "otp_Type_id", nullable = false)
    private int otpTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "otp_Type_id",
            insertable = false,
            updatable = false
    )
    private OtpType otpType;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "remaining_attempts")
    private byte remainingAttempts = 3;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}
