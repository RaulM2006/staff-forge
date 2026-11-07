package com.staffforge.backend.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staffforge.BackendApplication;
import com.staffforge.skill.Skill;
import com.staffforge.skill.SkillRepository;
import com.staffforge.skill.dto.SkillRequestDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User adminUser;
    private User employeeUser;
    private Skill savedSkill;

    @BeforeEach
    void setUp() {
        skillRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(new User(
                "admin@staffforge.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz123456",
                "Admin",
                "Super",
                Role.ADMIN
        ));

        employeeUser = userRepository.save(new User(
                "employee@staffforge.com",
                "$2a$10$abcdefghijklmnopqrstuvwxyz123456",
                "John",
                "Doe",
                Role.EMPLOYEE
        ));

        savedSkill = skillRepository.save(new Skill("Java", "Backend", "Core Java"));
    }

    @Nested
    @DisplayName("Public Endpoint Access Tests")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should allow unauthenticated GET request to /api/skills")
        void shouldAllowUnauthenticatedAccessToPublicEndpoint() throws Exception {
            mockMvc.perform(get("/api/skills"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Java")));
        }

        @Test
        @DisplayName("Should allow authenticated GET request with Bearer token to /api/skills")
        void shouldAllowAuthenticatedAccessToPublicEndpoint() throws Exception {
            String token = jwtUtils.generateToken(employeeUser.getEmail(), employeeUser.getRole().name());

            mockMvc.perform(get("/api/skills")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Java")));
        }
    }

    @Nested
    @DisplayName("Protected Endpoint Authentication & Authorization Tests")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("Should reject POST /api/skills with 401 when Authorization header is missing")
        void shouldRejectRequestWithoutToken() throws Exception {
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containers");

            mockMvc.perform(post("/api/skills")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject POST /api/skills with 401 when token is expired")
        void shouldRejectRequestWithExpiredToken() throws Exception {
            String expiredToken = jwtUtils.generateToken(adminUser.getEmail(), adminUser.getRole().name(), -60000);
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containers");

            mockMvc.perform(post("/api/skills")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + expiredToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject POST /api/skills with 401 when token is tampered/invalid")
        void shouldRejectRequestWithTamperedToken() throws Exception {
            String validToken = jwtUtils.generateToken(adminUser.getEmail(), adminUser.getRole().name());
            String tamperedToken = validToken.substring(0, validToken.length() - 6) + "XXXXXX";
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containers");

            mockMvc.perform(post("/api/skills")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + tamperedToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject POST /api/skills with 403 Forbidden when user has EMPLOYEE role")
        void shouldRejectRequestWithInsufficientRole() throws Exception {
            String employeeToken = jwtUtils.generateToken(employeeUser.getEmail(), employeeUser.getRole().name());
            SkillRequestDto request = new SkillRequestDto("Docker", "DevOps", "Containers");

            mockMvc.perform(post("/api/skills")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow POST /api/skills with 201 Created when user has valid ADMIN token")
        void shouldAllowRequestWithValidAdminToken() throws Exception {
            String adminToken = jwtUtils.generateToken(adminUser.getEmail(), adminUser.getRole().name());
            SkillRequestDto request = new SkillRequestDto("Kubernetes", "DevOps", "Orchestration");

            mockMvc.perform(post("/api/skills")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("Kubernetes")))
                    .andExpect(jsonPath("$.category", is("DevOps")));
        }

        @Test
        @DisplayName("Should allow PUT /api/skills/{id} with 200 OK when user has valid ADMIN token")
        void shouldAllowUpdateWithValidAdminToken() throws Exception {
            String adminToken = jwtUtils.generateToken(adminUser.getEmail(), adminUser.getRole().name());
            SkillRequestDto request = new SkillRequestDto("Java 21", "Backend", "Updated Java");

            mockMvc.perform(put("/api/skills/" + savedSkill.getId())
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Java 21")));
        }

        @Test
        @DisplayName("Should allow DELETE /api/skills/{id} with 204 No Content when user has valid ADMIN token")
        void shouldAllowDeleteWithValidAdminToken() throws Exception {
            String adminToken = jwtUtils.generateToken(adminUser.getEmail(), adminUser.getRole().name());

            mockMvc.perform(delete("/api/skills/" + savedSkill.getId())
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }
    }
}
