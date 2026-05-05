package com.ls.spaceBookingSystem.database.repository;

import com.ls.spaceBookingSystem.database.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.database.entity.AvailabilityRulePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, AvailabilityRulePrimaryKey> {
    public List<AvailabilityRule> findByIdTemplateId(long templateId);
}
