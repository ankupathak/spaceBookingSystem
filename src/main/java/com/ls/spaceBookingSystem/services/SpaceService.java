package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.dtos.requests.CreateSpaceRequest;
import com.ls.spaceBookingSystem.entity.Space;
import com.ls.spaceBookingSystem.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class SpaceService {
    @Autowired
    SpaceRepository spaceRepository;

    public void createSpace(CreateSpaceRequest data) {
        Space spaceEntity = new Space();
        spaceEntity.setName(data.getName());
        spaceEntity.setDescription(data.getDescription());
        spaceRepository.save(spaceEntity);
    }
}
