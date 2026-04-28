package com.ls.spaceBookingSystem.testData;

import com.ls.spaceBookingSystem.entity.AvailabilityTemplate;

public class AvailabilityTestData {
    public static AvailabilityTemplate buildTemplate(
            Long id, String name) {
        AvailabilityTemplate t = new AvailabilityTemplate();
        t.setTemplateId(id);
        t.setUserId(1L);
        if(name == null) t.setName("Morning");
        else t.setName(name);
        t.setMinBookingMinutes(30);
        t.setMaxBookingMinutes(90);
        t.setBufferMinutes(5);
        t.setRulesVersion(0);
        return t;
    }
}
