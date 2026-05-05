package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AuthDeviceId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private String deviceId;
}
