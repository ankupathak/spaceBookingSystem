package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "otp_types")
public class OtpType {

    @Id
    @Column(name = "otp_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int otpTypeId;

    @Column(name = "code")
    private String code;
}
