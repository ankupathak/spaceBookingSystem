package com.ls.spaceBookingSystem.database.repository;

import com.ls.spaceBookingSystem.database.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Query(value = "SELECT COUNT(o.otp_id) " +
        "FROM otps o " +
        "WHERE o.user_id = :userId " +
        "AND o.otp_type_id = :otpTypeId " +
        "AND o.created_at > :fromTime",   // ← column name not field name
        nativeQuery = true)
    int countOtpsSentInWindow(
        @Param("userId") Long userId,
        @Param("otpTypeId") int otpTypeId,
        @Param("fromTime") Instant fromTime
    );

    @Query(value = "SELECT * FROM otps WHERE user_id = :userId " +
            "AND otp_type_id = :otpTypeId " +
            "AND remaining_attempts > 0 " +
            "AND expired_at > :expiredAt " +
            "ORDER BY created_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<Otp> findLatestOtp(@Param("userId") long userId,
                                @Param("otpTypeId") int otpTypeId,
                                @Param("expiredAt") Instant expiredAt);

    @Modifying
    @Query(value = "UPDATE otps SET remaining_attempts = :remainingAttempts WHERE otp_id = :otpId",
            nativeQuery = true)
    void decrementAttempts(@Param("remainingAttempts") byte remainingAttempts, @Param("otpId") Long otpId);

}
