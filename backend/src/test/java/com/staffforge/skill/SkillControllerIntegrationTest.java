package com.staffforge.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staffforge.BackendApplication;
import com.staffforge.skill.dto.SkillRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Transactional
class SkillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private SkillRepository skillRepository;

    private Skill savedSkill;

    @BeforeEach
    void setUp() {
        skillRepository.deleteAll();
        savedSkill = skillRepository.save(new Skill("Java", "Backend", "Core Java"));
    }

    @Nested
    @DisplayName("GET /api/skills Tests")
    class GetAllSkillsEndpointTests {

        @Test
        @DisplayName("Unauthenticated users can list all skills")
        void shouldAllowUnauthenticatedUsersToGetAllSkills() throws Exception {
            mockMvc.perform(get("/api/skills"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Java")))
                    .andExpect(jsonPath("$[0].category", is("Backend")));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Authenticated non-admin users can list all skills")
        void shouldAllowAuthenticatedNonAdminUsersToGetAllSkills() throws Exception {
            mockMvc.perform(get("/api/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Java")));
        }
    }

    @Nested
    @DisplayName("POST /api/skills Security & Validation Tests")
    class CreateSkillEndpointTests {

        @Test
        @DisplayName("Unauthenticated user cannot create skill (401 Unauthorized)")
        void shouldRejectUnauthenticatedPost() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containerization");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Non-admin user (EMPLOYEE) cannot create skill (403 Forbidden)")
        void shouldRejectNonAdminPost() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containerization");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "PROJECT_MANAGER")
        @DisplayName("Non-admin user (PROJECT_MANAGER) cannot create skill (403 Forbidden)")
        void shouldRejectProjectManagerPost() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containerization");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user can create skill (201 Created)")
        void shouldAllowAdminToCreateSkill() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containerization");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("Docker")))
                    .andExpect(jsonPath("$.category", is("DevOps")))
                    .andExpect(jsonPath("$.description", is("Containerization")))
                    .andExpect(jsonPath("$.createdAt", notNullValue()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user cannot create skill with blank name (400 Bad Request)")
        void shouldRejectBlankName() throws Exception {
            SkillRequestDto request = new SkillRequestDto("", "DevOps", "Containerization");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.fieldErrors.name", notNullValue()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user cannot create skill with duplicate name (409 Conflict)")
        void shouldRejectDuplicateSkillName() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Java", "Backend", "Duplicate skill");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.message", containsString("already exists")));
        }
    }

    @Nested
    @DisplayName("PUT /api/skills/{id} Security & Validation Tests")
    class UpdateSkillEndpointTests {

        @Test
        @DisplayName("Unauthenticated user cannot update skill (401 Unauthorized)")
        void shouldRejectUnauthenticatedPut() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Java 21", "Backend", "Updated");

            mockMvc.perform(put("/api/skills/" + savedSkill.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Non-admin user (HR) cannot update skill (403 Forbidden)")
        void shouldRejectNonAdminPut() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Java 21", "Backend", "Updated");

            mockMvc.perform(put("/api/skills/" + savedSkill.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user can update existing skill (200 OK)")
        void shouldAllowAdminToUpdateSkill() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Java 21", "Backend / Core", "Updated Java");

            mockMvc.perform(put("/api/skills/" + savedSkill.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(savedSkill.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Java 21")))
                    .andExpect(jsonPath("$.category", is("Backend / Core")))
                    .andExpect(jsonPath("$.description", is("Updated Java")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user receives 404 when updating non-existent skill")
        void shouldReturn404WhenUpdatingNonExistentSkill() throws Exception {
            SkillRequestDto request = new SkillRequestDto("NonExistent", "Misc", "Does not exist");

            mockMvc.perform(put("/api/skills/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user cannot update skill with blank name (400 Bad Request)")
        void shouldRejectBlankNameOnUpdate() throws Exception {
            SkillRequestDto request = new SkillRequestDto("   ", "Misc", "Blank name");

            mockMvc.perform(put("/api/skills/" + savedSkill.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/skills/{id} Security Tests")
    class DeleteSkillEndpointTests {

        @Test
        @DisplayName("Unauthenticated user cannot delete skill (401 Unauthorized)")
        void shouldRejectUnauthenticatedDelete() throws Exception {
            mockMvc.perform(delete("/api/skills/" + savedSkill.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "RESOURCE_MANAGER")
        @DisplayName("Non-admin user cannot delete skill (403 Forbidden)")
        void shouldRejectNonAdminDelete() throws Exception {
            mockMvc.perform(delete("/api/skills/" + savedSkill.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin user can delete skill (204 No Content)")
        void shouldAllowAdminToDeleteSkill() throws Exception {
            mockMvc.perform(delete("/api/skills/" + savedSkill.getId()))
                    .andExpect(status().isNoContent());

            assertFalse(skillRepository.findById(savedSkill.getId()).isPresent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin receives 404 when deleting non-existent skill")
        void shouldReturn404WhenDeletingNonExistentSkill() throws Exception {
            mockMvc.perform(delete("/api/skills/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }
}
