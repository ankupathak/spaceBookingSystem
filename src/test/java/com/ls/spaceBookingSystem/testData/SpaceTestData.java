package com.ls.spaceBookingSystem.testData;

import com.ls.spaceBookingSystem.dtos.requests.CreateSpaceRequestDto;
import com.ls.spaceBookingSystem.dtos.requests.UpdateSpaceRequestDto;
import com.ls.spaceBookingSystem.entity.Space;

public class SpaceTestData {

    public static Space buildSpace() {
        Space s = new Space();
        s.setSpaceId(1L);
        s.setUserId(1L);
        s.setName("Test Space");
        s.setDescription("A test space");
        s.setActive(true);
        return s;
    }

    public static CreateSpaceRequestDto buildCreateRequest() {
        CreateSpaceRequestDto r = new CreateSpaceRequestDto();
        r.setName("Test Space");
        r.setDescription("A test space");
        r.setTemplateId(1L);
        return r;
    }

    public static UpdateSpaceRequestDto buildUpdateRequest(
        String name, String description, Long templateId) {
        UpdateSpaceRequestDto r = new UpdateSpaceRequestDto();
        r.setName(name);
        r.setDescription(description);
        r.setTemplateId(templateId);
        return r;
    }
}
