package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.entity.AvailabilityRulePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, AvailabilityRulePrimaryKey> {
//    public List<AvailabilityRule> findByIdTemplateIdAndDayOfWeek(long templateId, )
}
