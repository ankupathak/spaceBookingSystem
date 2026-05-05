package com.ls.spaceBookingSystem.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ls.spaceBookingSystem.database.entity.TimeSlotRange;
import com.ls.spaceBookingSystem.dtos.requests.CreateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateRulesRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.TimeRangeRequest;
import com.ls.spaceBookingSystem.dtos.responses.CreateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.UpdateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityRuleResponseDto;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityTemplateDto;
import com.ls.spaceBookingSystem.database.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.database.entity.AvailabilityRulePrimaryKey;
import com.ls.spaceBookingSystem.database.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.common.errors.ErrorCode;
import com.ls.spaceBookingSystem.common.exceptions.AppException;
import com.ls.spaceBookingSystem.database.repository.AvailabilityRuleRepository;
import com.ls.spaceBookingSystem.database.repository.AvailabilityTemplateRepository;
import com.ls.spaceBookingSystem.mapper.AvailabilityRuleMapper;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import com.ls.spaceBookingSystem.common.validations.validators.AvailabilityValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private  ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private AvailabilityValidator validator;

    @Autowired
    private AvailabilityTemplateRepository availabilityTemplateRepository;

    @Autowired
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Autowired
    private AvailabilityRuleMapper availabilityRuleMapper;

    public List<AvailabilityTemplateDto> getTemplates() {

        AccessTokenData authData = authService.getLoggedInUserData();

        List<AvailabilityTemplate> list = availabilityTemplateRepository.findByUserId(authData.getUserId());

        List<AvailabilityTemplateDto> response = new ArrayList<>();
        for(AvailabilityTemplate t: list) {
            response.add(AvailabilityTemplateDto.builder()
                    .templateId(t.getTemplateId())
                    .name(t.getName())
                    .minDuration(t.getMinBookingMinutes())
                    .maxDuration(t.getMaxBookingMinutes())
                    .bufferMinutes(t.getBufferMinutes())
                    .build());
        }
        return response;
    }

    public AvailabilityTemplateDto getTemplate(Long templateId) {

        AccessTokenData authData = authService.getLoggedInUserData();

        AvailabilityTemplate t = availabilityTemplateRepository.findByTemplateIdAndUserId(templateId,authData.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY,"No template found"));

        return AvailabilityTemplateDto.builder()
                .templateId(t.getTemplateId())
                .name(t.getName())
                .minDuration(t.getMinBookingMinutes())
                .maxDuration(t.getMaxBookingMinutes())
                .bufferMinutes(t.getBufferMinutes())
                .build();
    }

    public List<AvailabilityRuleResponseDto> getTemplateRules(Long templateId) {

        AccessTokenData authData = authService.getLoggedInUserData();

        AvailabilityTemplate t = availabilityTemplateRepository.findByTemplateIdAndUserId(templateId,authData.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY,"No template found"));

        List<AvailabilityRule> rules = availabilityRuleRepository.findByIdTemplateId(templateId);

        return rules.stream().map(availabilityRuleMapper::toResponse).toList();
    }

    @Transactional
    public CreateTemplateResponse createTemplate(CreateTemplateRequest data) {

        AccessTokenData authData = authService.getLoggedInUserData();

        validator.validateMinMaxDuration(data.getMinDuration(), data.getMaxDuration());
        validator.validateRules(data.getRules().getRules());

        AvailabilityTemplate t = new AvailabilityTemplate();
        t.setUserId(authData.getUserId());
        t.setName(data.getName());
        t.setMinBookingMinutes(data.getMinDuration());
        t.setMaxBookingMinutes(data.getMaxDuration());
        t.setBufferMinutes(data.getBufferMinutes());

        availabilityTemplateRepository.save(t);

        List<AvailabilityRule> list = new ArrayList<>();
        saveAllRules(data.getRules().getRules(),t.getTemplateId(), list);

        CreateTemplateResponse response = new CreateTemplateResponse();
        response.setTemplateId(t.getTemplateId());
        response.setName(t.getName());
        return response;
    }

    @Transactional
    public UpdateTemplateResponse updateTemplate(Long templateId, UpdateTemplateRequest data) {

        validator.validateMinMaxDuration(data.getMinDuration(), data.getMaxDuration());
        AccessTokenData authData = authService.getLoggedInUserData();

        AvailabilityTemplate t = availabilityTemplateRepository.findByTemplateIdAndUserId(templateId,authData.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY,"No template found"));

        t.setName(data.getName());
        t.setMinBookingMinutes(data.getMinDuration());
        t.setMaxBookingMinutes(data.getMaxDuration());
        t.setBufferMinutes(data.getBufferMinutes());

        availabilityTemplateRepository.save(t);

        UpdateTemplateResponse response = new UpdateTemplateResponse();
        response.setTemplateId(t.getTemplateId());
        response.setName(t.getName());
        return response;
    }

    @Transactional
    public void updateRules(Long templateId, UpdateRulesRequest data) {

        validator.validateRules(data.getRules().getRules());
        AccessTokenData authData = authService.getLoggedInUserData();

        AvailabilityTemplate t = availabilityTemplateRepository.findByTemplateIdAndUserId(templateId,authData.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AVAILABILITY,"No template found"));

        List<AvailabilityRule> list = new ArrayList<>();
        saveAllRules(data.getRules().getRules(),t.getTemplateId(),list);
        t.setRulesVersion(t.getRulesVersion()+1);
        availabilityTemplateRepository.save(t);
    }

    @Transactional
    private void saveAllRules(List<AvailabilityRuleDto> rules, long templateId, List<AvailabilityRule> list) {

        for (AvailabilityRuleDto r : rules) {

            AvailabilityRule ar = new AvailabilityRule();
            AvailabilityRulePrimaryKey id = new AvailabilityRulePrimaryKey();
                id.setTemplateId(templateId);
                id.setDayOfWeek(r.getDayOfWeek());
                ar.setId(id);
            List<TimeSlotRange> slots = new ArrayList<>();
                for(TimeRangeRequest slot: r.getSlots()) {
                    slots.add(new TimeSlotRange(slot.getStart().format(TIME_FMT),slot.getEnd().format(TIME_FMT)));
                }
                ar.setSlots(slots);

            list.add(ar);
        }

        availabilityRuleRepository.saveAll(list);
    }
}
