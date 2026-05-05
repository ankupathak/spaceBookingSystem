package com.ls.spaceBookingSystem.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseTimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false, length = 25)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(name = "password_hash", length = 60)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<UserRole> roles = new ArrayList<>();

    @Column(name = "tokens_valid_after", nullable = false)
    private Instant tokenValidAfter = Instant.now();

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";
}
