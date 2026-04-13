package com.ls.spaceBookingSystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseTimeStamp {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false, length = 25)
    private String email;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    @Column(name = "password_hash", length = 60)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<UserRole> roles = new ArrayList<>();

    @Column(name = "tokens_valid_after", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant tokenValidAfter;
}
