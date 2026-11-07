package com.staffforge.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staffforge.BackendApplication;
import com.staffforge.auth.dto.LoginRequestDto;
import com.staffforge.auth.dto.RegisterRequestDto;
import com.staffforge.backend.security.jwt.JwtAuthenticationFilter;
import com.staffforge.backend.security.jwt.JwtUtils;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        existingUser = userRepository.save(new User(
                "existing@staffforge.dev",
                passwordEncoder.encode("Secret123!"),
                "Existing",
                "User",
                Role.PROJECT_MANAGER
        ));
    }

    @Nested
    @DisplayName("POST /api/auth/register Tests")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 Created and JWT auth response on valid registration")
        void shouldRegisterNewUserSuccessfully() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                    "newuser@staffforge.dev",
                    "Password123!",
                    "Alice",
                    "Smith",
                    Role.EMPLOYEE
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.user.id", notNullValue()))
                    .andExpect(jsonPath("$.user.email", is("newuser@staffforge.dev")))
                    .andExpect(jsonPath("$.user.firstName", is("Alice")))
                    .andExpect(jsonPath("$.user.lastName", is("Smith")))
                    .andExpect(jsonPath("$.user.role", is("EMPLOYEE")))
                    .andExpect(jsonPath("$.user.roles", hasItem("ROLE_EMPLOYEE")));

            User saved = userRepository.findByEmail("newuser@staffforge.dev").orElseThrow();
            assertTrue(passwordEncoder.matches("Password123!", saved.getPassword()));
        }

        @Test
        @DisplayName("Should default to EMPLOYEE role when role is null in registration request")
        void shouldDefaultToEmployeeRole() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                    "defaultrole@staffforge.dev",
                    "Password123!",
                    "Bob",
                    "Jones",
                    null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.role", is("EMPLOYEE")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when validation fails (invalid email, short password)")
        void shouldRejectInvalidRegistrationPayload() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                    "not-an-email",
                    "123",
                    "",
                    "",
                    null
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.details", notNullValue()));
        }

        @Test
        @DisplayName("Should return 409 Conflict when registering with already used email")
        void shouldRejectDuplicateEmailRegistration() throws Exception {
            RegisterRequestDto request = new RegisterRequestDto(
                    "existing@staffforge.dev",
                    "AnotherPassword123!",
                    "Duplicate",
                    "User",
                    Role.EMPLOYEE
            );

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.message", containsString("already registered")));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login Tests")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 OK and JWT auth response on valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequestDto request = new LoginRequestDto("existing@staffforge.dev", "Secret123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.user.id", is(existingUser.getId().intValue())))
                    .andExpect(jsonPath("$.user.email", is("existing@staffforge.dev")))
                    .andExpect(jsonPath("$.user.role", is("PROJECT_MANAGER")))
                    .andExpect(jsonPath("$.user.roles", hasItem("ROLE_PROJECT_MANAGER")));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when password does not match")
        void shouldRejectWrongPassword() throws Exception {
            LoginRequestDto request = new LoginRequestDto("existing@staffforge.dev", "WrongPassword!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status", is(401)))
                    .andExpect(jsonPath("$.message", is("Invalid email or password")));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when user email does not exist")
        void shouldRejectNonExistentEmail() throws Exception {
            LoginRequestDto request = new LoginRequestDto("nonexistent@staffforge.dev", "Secret123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status", is(401)));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request body is blank or malformed")
        void shouldRejectBlankLoginRequest() throws Exception {
            LoginRequestDto request = new LoginRequestDto("", "");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me Tests")
    class CurrentUserEndpointTests {

        @Test
        @DisplayName("Should return 200 OK and user details when request has valid JWT Bearer token")
        void shouldReturnAuthenticatedUserDetails() throws Exception {
            String token = jwtUtils.generateToken(existingUser.getEmail(), existingUser.getRole().name());

            mockMvc.perform(get("/api/auth/me")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                    .andExpect(jsonPath("$.email", is("existing@staffforge.dev")))
                    .andExpect(jsonPath("$.firstName", is("Existing")))
                    .andExpect(jsonPath("$.lastName", is("User")))
                    .andExpect(jsonPath("$.role", is("PROJECT_MANAGER")))
                    .andExpect(jsonPath("$.roles", hasItems("ROLE_PROJECT_MANAGER", "PROJECT_MANAGER")))
                    .andExpect(jsonPath("$.enabled", is(true)));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when no Authorization header is provided")
        void shouldRejectUnauthenticatedRequest() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when Bearer token is expired")
        void shouldRejectExpiredToken() throws Exception {
            String expiredToken = jwtUtils.generateToken(existingUser.getEmail(), existingUser.getRole().name(), -60000);

            mockMvc.perform(get("/api/auth/me")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when Bearer token is tampered")
        void shouldRejectTamperedToken() throws Exception {
            String validToken = jwtUtils.generateToken(existingUser.getEmail(), existingUser.getRole().name());
            String tamperedToken = validToken.substring(0, validToken.length() - 5) + "ABCDE";

            mockMvc.perform(get("/api/auth/me")
                            .header(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + tamperedToken))
                    .andExpect(status().isUnauthorized());
        }
    }
}
