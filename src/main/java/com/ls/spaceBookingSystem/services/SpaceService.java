package com.ls.spaceBookingSystem.services;

import com.ls.spaceBookingSystem.dtos.requests.CreateSpaceRequestDto;
import com.ls.spaceBookingSystem.dtos.requests.UpdateSpaceRequestDto;
import com.ls.spaceBookingSystem.dtos.responses.SpaceResponseDto;
import com.ls.spaceBookingSystem.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.entity.Space;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.AvailabilityTemplateRepository;
import com.ls.spaceBookingSystem.repository.SpaceRepository;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;


@Service
public class SpaceService {

    @Autowired
    AuthService authService;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    AvailabilityTemplateRepository availabilityTemplateRepository;

    public Page<SpaceResponseDto> getSpaces(String name, int page, int size) {
        AccessTokenData auth = authService.getLoggedInUserData();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return spaceRepository
                .findByUserIdAndNameContaining(auth.getUserId(), name, pageable)
                .map(this::toSpaceResponseDto);
    }

    public SpaceResponseDto getSpace(Long spaceId) {
        AccessTokenData auth = authService.getLoggedInUserData();

        Space space = spaceRepository.findBySpaceIdAndUserId(spaceId, auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));
        return toSpaceResponseDto(space);
    }

    @Transactional
    public SpaceResponseDto createSpace(CreateSpaceRequestDto data) {
        AccessTokenData auth = authService.getLoggedInUserData();

        AvailabilityTemplate availabilityTemplate = availabilityTemplateRepository.findByTemplateIdAndUserId(data.getTemplateId(), auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        Space spaceEntity = new Space();
        spaceEntity.setUserId(auth.getUserId());
        spaceEntity.setTemplateId(availabilityTemplate.getTemplateId());
        spaceEntity.setName(data.getName());
        spaceEntity.setDescription(data.getDescription());
        return toSpaceResponseDto(spaceRepository.save(spaceEntity));
    }

    @Transactional
    public SpaceResponseDto updateSpace(Long spaceId, UpdateSpaceRequestDto data) {
        AccessTokenData auth = authService.getLoggedInUserData();


        Space space = spaceRepository.findBySpaceIdAndUserId(spaceId, auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        AvailabilityTemplate availabilityTemplate = availabilityTemplateRepository.findByTemplateIdAndUserId(data.getTemplateId(), auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND));

        space.setName(data.getName());
        space.setDescription(data.getDescription());
        space.setTemplateId(availabilityTemplate.getTemplateId());
        spaceRepository.save(space);
        return toSpaceResponseDto(space);


    }

    @Transactional
    public SpaceResponseDto toggleActive(Long spaceId) {
        AccessTokenData auth = authService.getLoggedInUserData();

        Space space = spaceRepository.findBySpaceIdAndUserId(spaceId, auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        boolean newStatus = !space.isActive();

        int updated = spaceRepository.updateActiveStatus(spaceId, auth.getUserId(), newStatus);
        if (updated == 0) {
            throw new AppException(ErrorCode.SPACE_NOT_FOUND);
        }

        space.setActive(newStatus);
        return toSpaceResponseDto(space);
    }

    @Transactional
    public void deleteSpace(Long spaceId) {
        AccessTokenData auth = authService.getLoggedInUserData();

        Space space = spaceRepository.findBySpaceIdAndUserId(spaceId, auth.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));
        spaceRepository.delete(space);
    }

    private SpaceResponseDto toSpaceResponseDto(Space space) {
        return SpaceResponseDto.builder()
                .spaceId(space.getSpaceId())
                .name(space.getName())
                .description(space.getDescription())
                .templateId(space.getTemplateId())
                .active(space.isActive())
                .build();
    }
}
