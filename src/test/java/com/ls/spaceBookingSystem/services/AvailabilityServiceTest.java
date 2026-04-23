package com.ls.spaceBookingSystem.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ls.spaceBookingSystem.dtos.requests.CreateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateRulesRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRuleDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.AvailabilityRulesDto;
import com.ls.spaceBookingSystem.dtos.requests.availability.TimeRangeRequest;
import com.ls.spaceBookingSystem.dtos.responses.CreateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.UpdateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityTemplateDto;
import com.ls.spaceBookingSystem.entity.AvailabilityTemplate;
import com.ls.spaceBookingSystem.entity.DayOfWeekEnum;
import com.ls.spaceBookingSystem.errors.ErrorCode;
import com.ls.spaceBookingSystem.exceptions.AppException;
import com.ls.spaceBookingSystem.repository.AvailabilityRuleRepository;
import com.ls.spaceBookingSystem.repository.AvailabilityTemplateRepository;
import com.ls.spaceBookingSystem.services.jwt.data.AccessTokenData;
import com.ls.spaceBookingSystem.validations.AvailabilityValidator;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceTest {
    @InjectMocks
    private AvailabilityService availabilityService;

    @Mock
    private AuthService authService;

    @Mock
    private AvailabilityValidator validator;

    @Mock
    private AvailabilityTemplateRepository availabilityTemplateRepository;

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private  ObjectMapper objectMapper;

    private AccessTokenData authData;
    @BeforeEach
    void setUp() {
        authData = new AccessTokenData();
        authData.setUserId(1L);
        when(authService.getLoggedInUserData()).thenReturn(authData);
    }

    @Nested
    class GetTemplatesTests {

        @Test
        void getTemplates_withResults_shouldReturnMappedDtos() {
            List<AvailabilityTemplate> templates =List.of(
                    buildTemplate(1L, null),
                    buildTemplate(1L,"Afternoon")
            );

            when(availabilityTemplateRepository.findByUserId(1L)).thenReturn(templates);

            List<AvailabilityTemplateDto> result = availabilityService.getTemplates();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTemplateId()).isEqualTo(1L);
            assertThat(result.get(0).getName()).isEqualTo("Morning");
            assertThat(result.get(0).getMinDuration()).isEqualTo(30);
            assertThat(result.get(0).getMaxDuration()).isEqualTo(90);
            assertThat(result.get(0).getBufferMinutes()).isEqualTo(5);
        }

        @Test
        void getTemplates_noResults_shouldReturnEmptyList() {
            when(availabilityTemplateRepository.findByUserId(1L)).thenReturn(List.of());

            List<AvailabilityTemplateDto> result = availabilityService.getTemplates();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetTemplateTests {

        @Test
        void getTemplate_existingTemplate_shouldReturnDto() {
            AvailabilityTemplate template = buildTemplate(1L,null);

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(template));

            AvailabilityTemplateDto result = availabilityService.getTemplate(1L);

            assertThat(result.getTemplateId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Morning");
            assertThat(result.getMinDuration()).isEqualTo(30);
            assertThat(result.getMaxDuration()).isEqualTo(90);
            assertThat(result.getBufferMinutes()).isEqualTo(5);
        }

        @Test
        void getTemplate_notFound_shouldThrow() {
            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> availabilityService.getTemplate(1L))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AVAILABILITY);
        }
    }

    @Nested
    class CreateTemplateTests {
        private CreateTemplateRequest requestData;

        @BeforeEach
        void setupCreateTemplateDto() {
            requestData = new CreateTemplateRequest();
            AvailabilityTemplate t = buildTemplate(1L,null);
            requestData.setName(t.getName());
            requestData.setMinDuration(t.getMinBookingMinutes());
            requestData.setMaxDuration(t.getMaxBookingMinutes());
            requestData.setBufferMinutes(t.getBufferMinutes());

            AvailabilityRulesDto rules = new AvailabilityRulesDto();

            AvailabilityRuleDto rule = new AvailabilityRuleDto();
            rule.setDayOfWeek(DayOfWeekEnum.MONDAY);
            List<TimeRangeRequest> slots = new ArrayList<>();
            slots.add(new TimeRangeRequest(LocalTime.now(),LocalTime.now().plusMinutes(90)));
            rule.setSlots(slots);

            rules.setRules(List.of(rule));
            requestData.setRules(rules);
        }

        @Test
        void createTemplate_validRequest_shouldSaveAndReturnResponse() throws Exception {

            when(availabilityTemplateRepository.save(any(AvailabilityTemplate.class))).thenAnswer((argu) -> {
                AvailabilityTemplate t = argu.getArgument(0);
                t.setTemplateId(1L);
                return t;
            });
            when(objectMapper.writeValueAsString(any())).thenReturn("[]");

            CreateTemplateResponse result = availabilityService.createTemplate(requestData);

            assertThat(result.getTemplateId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Morning");

            verify(validator).validateMinMaxDuration(30, 90);
            verify(validator).validateRules(any());

            verify(availabilityTemplateRepository).save(argThat(t ->
                    t.getUserId().equals(authData.getUserId()) &&
                            t.getName().equals("Morning") &&
                            t.getMinBookingMinutes() == 30 &&
                            t.getMaxBookingMinutes() == 90 &&
                            t.getBufferMinutes() == 5
            ));

            verify(availabilityRuleRepository).saveAll(anyList());
        }

        static Stream<Arguments> createValidationFailureScenarios() {
            return Stream.of(
                    Arguments.of("min > max",  120, 30,  ErrorCode.AVAILABILITY),
                    Arguments.of("zero min",   0,   120, ErrorCode.AVAILABILITY),
                    Arguments.of("neg min",    -1,  120, ErrorCode.AVAILABILITY)
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("createValidationFailureScenarios")
        void createTemplate_validationFailures_shouldThrow(
                String scenario,
                int minDuration,
                int maxDuration,
                ErrorCode expected) throws Exception {

            requestData.setMinDuration(minDuration);
            requestData.setMaxDuration(maxDuration);

            doThrow(new AppException(expected))
                    .when(validator).validateMinMaxDuration(minDuration, maxDuration);

            assertThatThrownBy(() -> availabilityService.createTemplate(requestData))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(expected);

            verify(availabilityTemplateRepository, never()).save(any());
            verify(availabilityRuleRepository, never()).saveAll(any());
        }

        @Test
        void createTemplate_slotSerializationFails_shouldThrow() throws Exception {

            when(availabilityTemplateRepository.save(any(AvailabilityTemplate.class))).thenAnswer((argu) -> {
                AvailabilityTemplate t = argu.getArgument(0);
                t.setTemplateId(1L);
                return t;
            });
            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("fail") {});

            assertThatThrownBy(() -> availabilityService.createTemplate(requestData))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AVAILABILITY);
        }
    }

    @Nested
    class UpdateTemplateTests {
        private UpdateTemplateRequest requestData;

        @BeforeEach
        void setupUpdateTemplateDto() {
            requestData = new UpdateTemplateRequest();
            requestData.setName("Updated");
            requestData.setMinDuration(45);
            requestData.setMaxDuration(150);
            requestData.setBufferMinutes(20);
        }

        @Test
        void updateTemplate_existingTemplate_shouldUpdateAndReturn() {
            AvailabilityTemplate existing = buildTemplate(1L,null);

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(existing));
            when(availabilityTemplateRepository.save(any(AvailabilityTemplate.class))).thenReturn(existing);

            UpdateTemplateResponse result = availabilityService.updateTemplate(1L, requestData);
            assertThat(result.getTemplateId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Updated");

            verify(availabilityTemplateRepository).save(argThat(t ->
                    t.getName().equals("Updated") &&
                            t.getMinBookingMinutes() == 45 &&
                            t.getMaxBookingMinutes() == 150 &&
                            t.getBufferMinutes() == 20
            ));
        }

        static Stream<Arguments> updateFailureScenarios() {
            return Stream.of(
                    Arguments.of("template not found", false, ErrorCode.AVAILABILITY),
                    Arguments.of("validation fails",  true,  ErrorCode.AVAILABILITY)
            );
        }
        @Test
        void updateTemplate_notFound_shouldThrow() {


            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> availabilityService.updateTemplate(1L, requestData))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AVAILABILITY);

            verify(availabilityTemplateRepository, never()).save(any());
        }

    }

    @Nested
    class UpdateRulesTests {
        private UpdateRulesRequest requestData;

        @BeforeEach
        void setupUpdateTemplateDto() {
            requestData = new UpdateRulesRequest();
            AvailabilityRulesDto rules = new AvailabilityRulesDto();

            AvailabilityRuleDto rule = new AvailabilityRuleDto();
            rule.setDayOfWeek(DayOfWeekEnum.MONDAY);
            List<TimeRangeRequest> slots = new ArrayList<>();
            slots.add(new TimeRangeRequest(LocalTime.now(),LocalTime.now().plusMinutes(90)));
            rule.setSlots(slots);

            rules.setRules(List.of(rule));
            requestData.setRules(rules);
        }

        @Test
        void updateRules_validRequest_shouldSaveRulesAndIncrementVersion() throws Exception {
//            UpdateRulesRequest request = buildUpdateRulesRequest();
            AvailabilityTemplate template = buildTemplate(1L, null);
            template.setRulesVersion(2);

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(template));
            when(objectMapper.writeValueAsString(any())).thenReturn("[]");

            availabilityService.updateRules(1L, requestData);

            // Rules saved
            verify(availabilityRuleRepository).saveAll(anyList());

            // Version incremented
            verify(availabilityTemplateRepository).save(argThat(t ->
                    t.getRulesVersion() == 3
            ));
        }

        @Test
        void updateRules_notFound_shouldThrow() throws Exception {


            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> availabilityService.updateRules(1L, requestData))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AVAILABILITY);

            verify(availabilityRuleRepository, never()).saveAll(any());
        }

        @Test
        void updateRules_slotSerializationFails_shouldThrow() throws Exception {

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(buildTemplate(1L, null)));

            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("fail") {});

            assertThatThrownBy(() -> availabilityService.updateRules(1L, requestData))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AVAILABILITY);
        }

    }


    private static AvailabilityTemplate buildTemplate(
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
