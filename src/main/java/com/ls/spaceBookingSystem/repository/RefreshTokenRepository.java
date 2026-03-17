package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByActiveJti(String activeJti);
    void deleteByActiveJti(String activeJti);

    @Modifying
    @Transactional
    void deleteByUserUserId(Long userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query(value = "UPDATE refresh_tokens r SET"+
        "r.active_jti  = :jti,"+
        "r.token_hash  = :tokenHash,"+
        "r.expires_at  = :expiresAt"+
        "WHERE r.id = :id" +
        "AND r.updated_at = :readAt", nativeQuery = true)
    int updateIfUnchanged(@Param("id") Long id,
                          @Param("jti") String jti,
                          @Param("tokenHash") String tokenHash,
                          @Param("expiresAt") LocalDateTime expiresAt,
                          @Param("readAt") LocalDateTime readAt);

}
