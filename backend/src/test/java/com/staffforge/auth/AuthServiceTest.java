package com.staffforge.auth;

import com.staffforge.auth.dto.AuthResponseDto;
import com.staffforge.auth.dto.LoginRequestDto;
import com.staffforge.auth.dto.RegisterRequestDto;
import com.staffforge.auth.dto.UserResponseDto;
import com.staffforge.auth.exception.EmailAlreadyExistsException;
import com.staffforge.auth.exception.UserNotFoundException;
import com.staffforge.backend.security.jwt.JwtUtils;
import com.staffforge.user.Role;
import com.staffforge.user.User;
import com.staffforge.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("john.doe@example.com", "encoded-pwd", "John", "Doe", Role.EMPLOYEE);
        sampleUser.setId(100L);
    }

    @Nested
    @DisplayName("register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register new user with default EMPLOYEE role and return token")
        void shouldRegisterSuccessfullyWithDefaultRole() {
            RegisterRequestDto request = new RegisterRequestDto(
                    "JOHN.DOE@EXAMPLE.COM ",
                    "plain-password",
                    "John",
                    "Doe",
                    null
            );

            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(passwordEncoder.encode("plain-password")).thenReturn("encoded-pwd");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(100L);
                return u;
            });
            when(jwtUtils.generateToken("john.doe@example.com", "EMPLOYEE")).thenReturn("mock-jwt-token");

            AuthResponseDto response = authService.register(request);

            assertNotNull(response);
            assertEquals("mock-jwt-token", response.getToken());
            assertEquals("Bearer", response.getType());
            assertNotNull(response.getUser());
            assertEquals(100L, response.getUser().getId());
            assertEquals("john.doe@example.com", response.getUser().getEmail());
            assertEquals("John", response.getUser().getFirstName());
            assertEquals("Doe", response.getUser().getLastName());
            assertEquals(Role.EMPLOYEE, response.getUser().getRole());
            assertTrue(response.getUser().getRoles().contains("ROLE_EMPLOYEE"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("john.doe@example.com", captor.getValue().getEmail());
            assertEquals("encoded-pwd", captor.getValue().getPassword());
            assertEquals(Role.EMPLOYEE, captor.getValue().getRole());
        }

        @Test
        @DisplayName("Should successfully register user with specified role (e.g., ADMIN)")
        void shouldRegisterWithSpecifiedRole() {
            RegisterRequestDto request = new RegisterRequestDto(
                    "admin@example.com",
                    "plain-password",
                    "Admin",
                    "Super",
                    Role.ADMIN
            );

            when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("plain-password")).thenReturn("encoded-pwd");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(200L);
                return u;
            });
            when(jwtUtils.generateToken("admin@example.com", "ADMIN")).thenReturn("admin-token");

            AuthResponseDto response = authService.register(request);

            assertEquals(Role.ADMIN, response.getUser().getRole());
            assertTrue(response.getUser().getRoles().contains("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is already registered")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequestDto request = new RegisterRequestDto(
                    "john.doe@example.com",
                    "plain-password",
                    "John",
                    "Doe",
                    Role.EMPLOYEE
            );

            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
            verify(userRepository, never()).save(any());
            verify(jwtUtils, never()).generateToken(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate and return JWT token and user info on valid credentials")
        void shouldLoginSuccessfully() {
            LoginRequestDto request = new LoginRequestDto("john.doe@example.com", "plain-password");

            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));
            when(jwtUtils.generateToken("john.doe@example.com", "EMPLOYEE")).thenReturn("jwt-token-123");

            AuthResponseDto response = authService.login(request);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            assertNotNull(response);
            assertEquals("jwt-token-123", response.getToken());
            assertEquals("john.doe@example.com", response.getUser().getEmail());
            assertEquals(Role.EMPLOYEE, response.getUser().getRole());
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when credentials are invalid")
        void shouldThrowWhenBadCredentials() {
            LoginRequestDto request = new LoginRequestDto("john.doe@example.com", "wrong-password");

            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtUtils, never()).generateToken(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException if user record missing after authentication")
        void shouldThrowWhenUserMissingInDb() {
            LoginRequestDto request = new LoginRequestDto("unknown@example.com", "password");

            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.login(request));
        }
    }

    @Nested
    @DisplayName("getCurrentUser Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return UserResponseDto when user exists")
        void shouldReturnCurrentUser() {
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));

            UserResponseDto dto = authService.getCurrentUser("john.doe@example.com");

            assertNotNull(dto);
            assertEquals(100L, dto.getId());
            assertEquals("john.doe@example.com", dto.getEmail());
            assertEquals("John", dto.getFirstName());
            assertEquals("Doe", dto.getLastName());
            assertEquals(Role.EMPLOYEE, dto.getRole());
            assertTrue(dto.getRoles().contains("ROLE_EMPLOYEE"));
            assertTrue(dto.isEnabled());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user is not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.getCurrentUser("missing@example.com"));
        }
    }
}
