package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.AuthDevice;
import com.ls.spaceBookingSystem.entity.AvailabilityTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvailabilityTemplateRepository extends JpaRepository<AvailabilityTemplate,Long> {
    Optional<AvailabilityTemplate> findByTemplateIdAndUserId(long templateId, long userId);

    List<AvailabilityTemplate> findByUserId(long userId);
}
