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
import com.ls.spaceBookingSystem.testData.AvailabilityTestData;
import com.ls.spaceBookingSystem.testData.SpaceTestData;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpaceServiceTest {
    @InjectMocks
    SpaceService spaceService;

    @Mock
    SpaceRepository spaceRepository;

    @Mock
    AuthService authService;

    @Mock
    AvailabilityTemplateRepository availabilityTemplateRepository;

    private AccessTokenData authData;
    @BeforeEach
    void setUp() {
        authData = new AccessTokenData();
        authData.setUserId(1L);
        when(authService.getLoggedInUserData()).thenReturn(authData);
    }

    @Nested
    class GetSpacesTests {

        @Test
        void getSpaces_withResults_shouldReturnMappedDtos() {
            Page<Space> page = new PageImpl<>(List.of(SpaceTestData.buildSpace(), SpaceTestData.buildSpace()));
            when(spaceRepository.findByUserIdAndNameContaining(anyLong(), any(), any(Pageable.class)))
                    .thenReturn(page);

            Page<SpaceResponseDto> result = spaceService.getSpaces(null, 0, 10);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Space");
        }

        @Test
        void getSpaces_withNameFilter_shouldPassFilterToRepo() {
            when(spaceRepository.findByUserIdAndNameContaining(anyLong(), any(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            spaceService.getSpaces("meeting", 0, 10);

            verify(spaceRepository).findByUserIdAndNameContaining(
                    eq(1L), eq("meeting"), any(Pageable.class));
        }
    }

    @Nested
    class GetSpaceTests {

        @Test
        void getSpace_ownSpace_shouldReturnDto() {
            when(spaceRepository.findBySpaceIdAndUserId(1L, 1L))
                    .thenReturn(Optional.of(SpaceTestData.buildSpace()));

            SpaceResponseDto result = spaceService.getSpace(1L);

            assertThat(result.getSpaceId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Space");
        }

        static Stream<Arguments> notFoundScenarios() {
            return Stream.of(
                    Arguments.of("space doesnt exist",         99L, 1L),
                    Arguments.of("space belongs to other user", 1L, 2L)
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("notFoundScenarios")
        void getSpace_notOwned_shouldThrow(String scenario, Long spaceId, Long userId) {
            authData.setUserId(userId);
            when(spaceRepository.findBySpaceIdAndUserId(spaceId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> spaceService.getSpace(spaceId))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SPACE_NOT_FOUND);
        }
    }

    @Nested
    class CreateSpaceTests {

        @Test
        void createSpace_shouldSaveAndReturn() {
            AvailabilityTemplate template = AvailabilityTestData.buildTemplate(1L,null);
            CreateSpaceRequestDto request = SpaceTestData.buildCreateRequest();

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(template));

            when(spaceRepository.save(any(Space.class)))
                    .thenAnswer(inv -> {
                        Space s = inv.getArgument(0);
                        s.setSpaceId(1L);
                        return s;
                    });

            SpaceResponseDto result = spaceService.createSpace(request);

            assertThat(result.getSpaceId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Space");
            assertThat(result.isActive()).isFalse();

            verify(spaceRepository).save(argThat(s ->
                    s.getUserId().equals(1L) &&
                            s.getName().equals("Test Space") &&
                            !s.isActive()
            ));
        }

        @Test
        void createSpace_withNoValidTemplate_shouldThrow() {
            CreateSpaceRequestDto request = SpaceTestData.buildCreateRequest();

            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> spaceService.createSpace(request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND);

            verify(spaceRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateSpaceTests {

        @Test
        void updateSpace_ownSpace_shouldUpdateFields() {
            AvailabilityTemplate template = AvailabilityTestData.buildTemplate(1L,null);
            UpdateSpaceRequestDto request = SpaceTestData.buildUpdateRequest("Updated", "New desc", 1L);
            Space existing = SpaceTestData.buildSpace();

            when(spaceRepository.findBySpaceIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(existing));
            when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(template));

            SpaceResponseDto result = spaceService.updateSpace(1L, request);

            assertThat(result.getName()).isEqualTo("Updated");
            assertThat(result.getDescription()).isEqualTo("New desc");
            verify(spaceRepository).save(argThat(s ->
                    s.getName().equals("Updated") &&
                            s.getDescription().equals("New desc")
            ));
        }

        static Stream<Arguments> updateFailureScenarios() {
            return Stream.of(
                    Arguments.of("space not found",        false, 1L, ErrorCode.SPACE_NOT_FOUND),
                    Arguments.of("template not found",     true, 5L,  ErrorCode.TEMPLATE_NOT_FOUND)
            );
        }
        @ParameterizedTest(name = "{0}")
        @MethodSource("updateFailureScenarios")
        void updateSpace_failures_shouldThrow(
                String scenario,
                boolean spaceExists,
                Long templateId,
                ErrorCode expected) {

            UpdateSpaceRequestDto request = SpaceTestData.buildUpdateRequest("Name", "Desc", templateId);

            if (!spaceExists) {
                when(spaceRepository.findBySpaceIdAndUserId(any(), any()))
                        .thenReturn(Optional.empty());
            } else {
                when(spaceRepository.findBySpaceIdAndUserId(any(), any()))
                        .thenReturn(Optional.of(SpaceTestData.buildSpace()));
                when(availabilityTemplateRepository.findByTemplateIdAndUserId(anyLong(), anyLong()))
                        .thenReturn(Optional.empty());
            }

            assertThatThrownBy(() -> spaceService.updateSpace(1L, request))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(expected);
        }
    }

    @Nested
    class DeleteSpaceTests {

        @Test
        void deleteSpace_ownSpace_shouldDelete() {
            Space space = SpaceTestData.buildSpace();
            when(spaceRepository.findBySpaceIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(space));

            assertThatNoException().isThrownBy(() -> spaceService.deleteSpace(1L));

            verify(spaceRepository).delete(space);
        }

        @Test
        void deleteSpace_notOwned_shouldThrow() {
            when(spaceRepository.findBySpaceIdAndUserId(any(), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> spaceService.deleteSpace(1L))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SPACE_NOT_FOUND);

            verify(spaceRepository, never()).delete(any());
        }
    }

    @Nested
    class ToggleActiveTests {

        static Stream<Arguments> toggleScenarios() {
            return Stream.of(
                    Arguments.of(true,  false),   // active → inactive
                    Arguments.of(false, true)     // inactive → active
            );
        }
        @ParameterizedTest(name = "currentActive={0} → newActive={1}")
        @MethodSource("toggleScenarios")
        void toggleActive_shouldFlipStatus(boolean currentActive, boolean expectedActive) {
            Space space = SpaceTestData.buildSpace();
            space.setActive(currentActive);

            when(spaceRepository.findBySpaceIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(space));
            when(spaceRepository.updateActiveStatus(anyLong(), anyLong(), anyBoolean()))
                    .thenReturn(1);

            SpaceResponseDto result = spaceService.toggleActive(1L);

            assertThat(result.isActive()).isEqualTo(expectedActive);
            verify(spaceRepository).updateActiveStatus(1L, 1L, expectedActive);
        }

        @Test
        void toggleActive_notOwned_shouldThrow() {
            when(spaceRepository.findBySpaceIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> spaceService.toggleActive(1L))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.SPACE_NOT_FOUND);
        }
    }
}
