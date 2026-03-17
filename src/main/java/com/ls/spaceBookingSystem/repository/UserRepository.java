package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIsEmailVerifiedTrue(String email);

    @Modifying
    @Query(value = "UPDATE users u SET u.token_version = u.token_version + 1 WHERE u.user_id = :userId",nativeQuery = true)
    void incrementTokenVersion(@Param("userId") Long userId);
}
