package com.ls.spaceBookingSystem.database.repository;

import com.ls.spaceBookingSystem.database.entity.Space;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    Optional<Space> findBySpaceIdAndUserId(Long spaceId, Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Space s SET s.isActive = :active WHERE s.spaceId = :spaceId AND s.userId = :userId")
    int updateActiveStatus(
            @Param("spaceId") Long spaceId,
            @Param("userId") Long userId,
            @Param("active") boolean active);

    @Query("SELECT s FROM Space s WHERE s.userId = :userId " +
            "AND (CASE WHEN :name IS NULL OR :name = '' THEN 1 ELSE 0 END = 1 " +
            "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Space> findByUserIdAndNameContaining(
            @Param("userId") Long userId,
            @Param("name") String name,
            Pageable pageable);

    @Query("""
        SELECT s FROM Space s
        JOIN FETCH s.template t
        JOIN FETCH t.rules
        WHERE s.spaceId = :spaceId
          AND s.isActive  = true
          AND t.isActive  = true
        """)
    Optional<Space> findActiveWithTemplateAndRules(@Param("spaceId") Long spaceId);
}