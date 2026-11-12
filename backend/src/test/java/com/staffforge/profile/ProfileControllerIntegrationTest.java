package com.staffforge.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staffforge.BackendApplication;
import com.staffforge.employee.EmployeeProfile;
import com.staffforge.employee.EmployeeProfileRepository;
import com.staffforge.employee.EmployeeSkill;
import com.staffforge.employee.EmployeeSkillRepository;
import com.staffforge.employee.ProficiencyLevel;
import com.staffforge.profile.dto.AddSkillRequestDto;
import com.staffforge.profile.dto.ProfileUpdateRequestDto;
import com.staffforge.project.SeniorityLevel;
import com.staffforge.skill.Skill;
import com.staffforge.skill.SkillRepository;
import com.staffforge.user.Role;
import com.staffforge.user.User;
import com.staffforge.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User testUser;
    private EmployeeProfile testProfile;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        employeeSkillRepository.deleteAll();
        employeeProfileRepository.deleteAll();
        userRepository.deleteAll();
        skillRepository.deleteAll();

        testUser = userRepository.save(new User(
                "profile.test@staffforge.dev",
                passwordEncoder.encode("Secret123!"),
                "Alex",
                "Morgan",
                Role.EMPLOYEE
        ));

        testProfile = new EmployeeProfile(
                testUser,
                "Backend Engineer",
                "Core Engineering",
                SeniorityLevel.MID,
                100,
                "Passionate developer"
        );
        testProfile = employeeProfileRepository.save(testProfile);

        testSkill = skillRepository.save(new Skill("PostgreSQL", "Database", "Relational database"));
    }

    @Nested
    @DisplayName("GET /api/profiles/me Tests")
    class GetMyProfileEndpointTests {

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 200 and profile when authenticated")
        void shouldReturnProfileWhenAuthenticated() throws Exception {
            mockMvc.perform(get("/api/profiles/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", is("profile.test@staffforge.dev")))
                    .andExpect(jsonPath("$.firstName", is("Alex")))
                    .andExpect(jsonPath("$.jobTitle", is("Backend Engineer")))
                    .andExpect(jsonPath("$.department", is("Core Engineering")))
                    .andExpect(jsonPath("$.seniorityLevel", is("MID")));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when unauthenticated")
        void shouldRejectUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/api/profiles/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/profiles/me Tests")
    class UpdateMyProfileEndpointTests {

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 200 and updated profile details")
        void shouldUpdateProfileSuccessfully() throws Exception {
            ProfileUpdateRequestDto updateDto = new ProfileUpdateRequestDto();
            ReflectionTestUtils.setField(updateDto, "jobTitle", "Lead Engineer");
            ReflectionTestUtils.setField(updateDto, "department", "Architecture");
            ReflectionTestUtils.setField(updateDto, "seniorityLevel", SeniorityLevel.LEAD);
            ReflectionTestUtils.setField(updateDto, "capacityPercentage", 90);
            ReflectionTestUtils.setField(updateDto, "bio", "Lead architect bio");

            mockMvc.perform(put("/api/profiles/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jobTitle", is("Lead Engineer")))
                    .andExpect(jsonPath("$.department", is("Architecture")))
                    .andExpect(jsonPath("$.seniorityLevel", is("LEAD")))
                    .andExpect(jsonPath("$.capacityPercentage", is(90)))
                    .andExpect(jsonPath("$.bio", is("Lead architect bio")));
        }

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 400 Bad Request on invalid capacity percentage")
        void shouldRejectInvalidCapacity() throws Exception {
            ProfileUpdateRequestDto updateDto = new ProfileUpdateRequestDto();
            ReflectionTestUtils.setField(updateDto, "jobTitle", "Engineer");
            ReflectionTestUtils.setField(updateDto, "seniorityLevel", SeniorityLevel.MID);
            ReflectionTestUtils.setField(updateDto, "capacityPercentage", 150); // > 100

            mockMvc.perform(put("/api/profiles/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }
    }

    @Nested
    @DisplayName("POST /api/profiles/me/skills Tests")
    class AddSkillEndpointTests {

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 201 Created when associating skill")
        void shouldAddSkillSuccessfully() throws Exception {
            AddSkillRequestDto request = new AddSkillRequestDto();
            ReflectionTestUtils.setField(request, "skillId", testSkill.getId());
            ReflectionTestUtils.setField(request, "proficiencyLevel", ProficiencyLevel.EXPERT);
            ReflectionTestUtils.setField(request, "yearsOfExperience", 5);

            mockMvc.perform(post("/api/profiles/me/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.skillId", is(testSkill.getId().intValue())))
                    .andExpect(jsonPath("$.skillName", is("PostgreSQL")))
                    .andExpect(jsonPath("$.proficiencyLevel", is("EXPERT")))
                    .andExpect(jsonPath("$.yearsOfExperience", is(5)));
        }

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 409 Conflict when skill is already associated")
        void shouldRejectDuplicateSkillAssociation() throws Exception {
            employeeSkillRepository.save(new EmployeeSkill(testProfile, testSkill, ProficiencyLevel.INTERMEDIATE, 2));

            AddSkillRequestDto request = new AddSkillRequestDto();
            ReflectionTestUtils.setField(request, "skillId", testSkill.getId());
            ReflectionTestUtils.setField(request, "proficiencyLevel", ProficiencyLevel.ADVANCED);
            ReflectionTestUtils.setField(request, "yearsOfExperience", 3);

            mockMvc.perform(post("/api/profiles/me/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/profiles/me/skills/{skillId} Tests")
    class RemoveSkillEndpointTests {

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 204 No Content when removing associated skill")
        void shouldRemoveSkillSuccessfully() throws Exception {
            employeeSkillRepository.save(new EmployeeSkill(testProfile, testSkill, ProficiencyLevel.BEGINNER, 1));

            mockMvc.perform(delete("/api/profiles/me/skills/" + testSkill.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "profile.test@staffforge.dev", roles = "EMPLOYEE")
        @DisplayName("Should return 404 when removing non-associated skill")
        void shouldReturn404WhenSkillNotAssociated() throws Exception {
            mockMvc.perform(delete("/api/profiles/me/skills/" + testSkill.getId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }
}
