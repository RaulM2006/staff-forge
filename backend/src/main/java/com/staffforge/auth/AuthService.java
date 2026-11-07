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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        Role role = request.getRole() != null ? request.getRole() : Role.EMPLOYEE;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                normalizedEmail,
                encodedPassword,
                request.getFirstName().trim(),
                request.getLastName().trim(),
                role
        );

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponseDto(token, UserResponseDto.fromEntity(savedUser));
    }

    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponseDto(token, UserResponseDto.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));

        return UserResponseDto.fromEntity(user);
    }
}
