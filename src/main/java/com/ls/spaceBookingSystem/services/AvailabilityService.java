package com.ls.spaceBookingSystem.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ls.spaceBookingSystem.dtos.requests.CreateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateRulesRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.responses.CreateAccountResponse;
import com.ls.spaceBookingSystem.dtos.responses.CreateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.UpdateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityTemplateDto;
import com.ls.spaceBookingSystem.entity.AvailabilityRule;
import com.ls.spaceBookingSystem.entity.AvailabilityRulePrimaryKey;
import com.ls.spaceBookingSystem.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.AvailabilityRuleRepository;
import com.ls.spaceBookingSystem.repository.AvailabilityTemplateRepository;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import com.ls.spaceBookingSystem.validations.AvailabilityValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

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
            id.setTemplate_id(templateId);
            id.setDayOfWeek(r.getDayOfWeek());
            ar.setId(id);
            try {
                ar.setSlots(objectMapper.writeValueAsString(r.getSlots()));
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.AVAILABILITY,"Something went wrong. Please try again.")
                        .withDevMessage("FAILED_TO_SERIALIZE_SLOTS");
            }

            list.add(ar);
        }

        availabilityRuleRepository.saveAll(list);
    }
}
