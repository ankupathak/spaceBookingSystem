package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.AuthDevice;
import com.ls.spaceBookingSystem.entity.AuthDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AuthDeviceRepository extends JpaRepository<AuthDevice, AuthDeviceId> {
    @Modifying
    @Transactional
    void deleteByIdDeviceIdAndIdUserId(String deviceId, Long userId);

    Optional<AuthDevice> findByIdDeviceIdAndIdUserId(String deviceId, long userId);

    @Modifying
    @Transactional
    @Query("""
    UPDATE AuthDevice ad
    SET ad.expiresAt = :expiresAt
    WHERE ad.id.deviceId = :deviceId
      AND ad.id.userId = :userId
""")
    int updateIfUnchanged(@Param("deviceId") String deviceId,
                          @Param("userId") Long userId,
                          @Param("expiresAt") Instant expiresAt);
}
