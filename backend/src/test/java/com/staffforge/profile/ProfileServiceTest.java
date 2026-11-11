package com.staffforge.profile;

import com.staffforge.auth.exception.UserNotFoundException;
import com.staffforge.employee.EmployeeProfile;
import com.staffforge.employee.EmployeeProfileRepository;
import com.staffforge.employee.EmployeeSkill;
import com.staffforge.employee.EmployeeSkillRepository;
import com.staffforge.employee.ProficiencyLevel;
import com.staffforge.profile.dto.AddSkillRequestDto;
import com.staffforge.profile.dto.EmployeeSkillResponseDto;
import com.staffforge.profile.dto.ProfileResponseDto;
import com.staffforge.profile.dto.ProfileUpdateRequestDto;
import com.staffforge.profile.exception.ProfileNotFoundException;
import com.staffforge.profile.exception.SkillAlreadyAssociatedException;
import com.staffforge.profile.exception.SkillNotAssociatedException;
import com.staffforge.project.SeniorityLevel;
import com.staffforge.skill.Skill;
import com.staffforge.skill.SkillRepository;
import com.staffforge.skill.exception.SkillNotFoundException;
import com.staffforge.user.Role;
import com.staffforge.user.User;
import com.staffforge.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeProfileRepository employeeProfileRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private EmployeeProfile profile;
    private Skill javaSkill;

    @BeforeEach
    void setUp() {
        user = new User("user@staffforge.dev", "encodedPassword", "John", "Doe", Role.EMPLOYEE);
        ReflectionTestUtils.setField(user, "id", 10L);

        profile = new EmployeeProfile(user, "Software Engineer", "Engineering", SeniorityLevel.MID, 100, "Developer bio");
        ReflectionTestUtils.setField(profile, "id", 20L);

        javaSkill = new Skill("Java", "Backend", "Core Java");
        ReflectionTestUtils.setField(javaSkill, "id", 30L);
    }

    @Nested
    @DisplayName("getMyProfile Tests")
    class GetMyProfileTests {

        @Test
        @DisplayName("Should return full profile when user and profile exist")
        void shouldReturnProfileWhenFound() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

            ProfileResponseDto result = profileService.getMyProfile("user@staffforge.dev");

            assertNotNull(result);
            assertEquals("Software Engineer", result.getJobTitle());
            assertEquals("Engineering", result.getDepartment());
            assertEquals("MID", result.getSeniorityLevel());
            assertEquals("John", result.getFirstName());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail("unknown@staffforge.dev")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> profileService.getMyProfile("unknown@staffforge.dev"));
        }

        @Test
        @DisplayName("Should throw ProfileNotFoundException when employee profile does not exist")
        void shouldThrowWhenProfileNotFound() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.empty());

            assertThrows(ProfileNotFoundException.class,
                    () -> profileService.getMyProfile("user@staffforge.dev"));
        }
    }

    @Nested
    @DisplayName("updateMyProfile Tests")
    class UpdateMyProfileTests {

        @Test
        @DisplayName("Should update basic details and save profile")
        void shouldUpdateProfileSuccessfully() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(employeeProfileRepository.save(any(EmployeeProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ProfileUpdateRequestDto updateDto = new ProfileUpdateRequestDto();
            ReflectionTestUtils.setField(updateDto, "jobTitle", "Senior Software Engineer");
            ReflectionTestUtils.setField(updateDto, "department", "Product");
            ReflectionTestUtils.setField(updateDto, "seniorityLevel", SeniorityLevel.SENIOR);
            ReflectionTestUtils.setField(updateDto, "capacityPercentage", 80);
            ReflectionTestUtils.setField(updateDto, "bio", "Updated bio");

            ProfileResponseDto updated = profileService.updateMyProfile("user@staffforge.dev", updateDto);

            assertNotNull(updated);
            assertEquals("Senior Software Engineer", updated.getJobTitle());
            assertEquals("Product", updated.getDepartment());
            assertEquals("SENIOR", updated.getSeniorityLevel());
            assertEquals(80, updated.getCapacityPercentage());
            assertEquals("Updated bio", updated.getBio());
            verify(employeeProfileRepository, times(1)).save(profile);
        }
    }

    @Nested
    @DisplayName("addSkillToMyProfile Tests")
    class AddSkillTests {

        @Test
        @DisplayName("Should successfully add skill to profile")
        void shouldAddSkillSuccessfully() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(employeeSkillRepository.existsByEmployeeProfileIdAndSkillId(20L, 30L)).thenReturn(false);
            when(skillRepository.findById(30L)).thenReturn(Optional.of(javaSkill));

            EmployeeSkill employeeSkill = new EmployeeSkill(profile, javaSkill, ProficiencyLevel.ADVANCED, 4);
            ReflectionTestUtils.setField(employeeSkill, "id", 50L);
            when(employeeSkillRepository.save(any(EmployeeSkill.class))).thenReturn(employeeSkill);

            AddSkillRequestDto request = new AddSkillRequestDto();
            ReflectionTestUtils.setField(request, "skillId", 30L);
            ReflectionTestUtils.setField(request, "proficiencyLevel", ProficiencyLevel.ADVANCED);
            ReflectionTestUtils.setField(request, "yearsOfExperience", 4);

            EmployeeSkillResponseDto result = profileService.addSkillToMyProfile("user@staffforge.dev", request);

            assertNotNull(result);
            assertEquals("Java", result.getSkillName());
            assertEquals("ADVANCED", result.getProficiencyLevel());
            assertEquals(4, result.getYearsOfExperience());
            verify(employeeSkillRepository, times(1)).save(any(EmployeeSkill.class));
        }

        @Test
        @DisplayName("Should throw SkillAlreadyAssociatedException if already added")
        void shouldThrowWhenSkillAlreadyAssociated() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(employeeSkillRepository.existsByEmployeeProfileIdAndSkillId(20L, 30L)).thenReturn(true);

            AddSkillRequestDto request = new AddSkillRequestDto();
            ReflectionTestUtils.setField(request, "skillId", 30L);

            assertThrows(SkillAlreadyAssociatedException.class,
                    () -> profileService.addSkillToMyProfile("user@staffforge.dev", request));
            verify(skillRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw SkillNotFoundException if skillId does not exist in catalog")
        void shouldThrowWhenSkillNotFound() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(employeeSkillRepository.existsByEmployeeProfileIdAndSkillId(20L, 999L)).thenReturn(false);
            when(skillRepository.findById(999L)).thenReturn(Optional.empty());

            AddSkillRequestDto request = new AddSkillRequestDto();
            ReflectionTestUtils.setField(request, "skillId", 999L);

            assertThrows(SkillNotFoundException.class,
                    () -> profileService.addSkillToMyProfile("user@staffforge.dev", request));
        }
    }

    @Nested
    @DisplayName("removeSkillFromMyProfile Tests")
    class RemoveSkillTests {

        @Test
        @DisplayName("Should remove skill from profile when associated")
        void shouldRemoveSkillSuccessfully() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

            EmployeeSkill employeeSkill = new EmployeeSkill(profile, javaSkill, ProficiencyLevel.ADVANCED, 3);
            when(employeeSkillRepository.findByEmployeeProfileIdAndSkillId(20L, 30L))
                    .thenReturn(Optional.of(employeeSkill));

            profileService.removeSkillFromMyProfile("user@staffforge.dev", 30L);

            verify(employeeSkillRepository, times(1)).delete(employeeSkill);
        }

        @Test
        @DisplayName("Should throw SkillNotAssociatedException when skill not attached to profile")
        void shouldThrowWhenSkillNotAssociated() {
            when(userRepository.findByEmail("user@staffforge.dev")).thenReturn(Optional.of(user));
            when(employeeProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
            when(employeeSkillRepository.findByEmployeeProfileIdAndSkillId(20L, 30L))
                    .thenReturn(Optional.empty());

            assertThrows(SkillNotAssociatedException.class,
                    () -> profileService.removeSkillFromMyProfile("user@staffforge.dev", 30L));
            verify(employeeSkillRepository, never()).delete(any());
        }
    }
}
