package com.staffforge.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should pass through when no Authorization header is present")
    void shouldPassThroughWhenNoAuthorizationHeaderPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("Should pass through when Authorization header does not start with Bearer")
    void shouldPassThroughWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("Should pass through without authenticating when token is invalid")
    void shouldPassThroughWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid.token.here";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtUtils).validateToken(token);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should authenticate user with UserDetails when token is valid and user exists in database")
    void shouldAuthenticateWithUserDetailsWhenTokenValidAndUserExists() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "john.doe@example.com";
        UserDetails userDetails = new User(
                email,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtils.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userDetails, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
    }

    @Test
    @DisplayName("Should authenticate with token claims when user is not found in database (fallback)")
    void shouldAuthenticateWithTokenClaimsWhenUserNotInDatabase() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "external.user@example.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenThrow(new UsernameNotFoundException("Not found"));
        when(jwtUtils.extractRole(token)).thenReturn("PROJECT_MANAGER");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROJECT_MANAGER")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PROJECT_MANAGER")));
    }

    @Test
    @DisplayName("Should authenticate with token claims when UserDetailsService is null")
    void shouldAuthenticateWithTokenClaimsWhenUserDetailsServiceIsNull() throws ServletException, IOException {
        JwtAuthenticationFilter filterWithoutUserDetailsService = new JwtAuthenticationFilter(jwtUtils, null);

        String token = "valid.jwt.token";
        String email = "external.user@example.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn(email);
        when(jwtUtils.extractRole(token)).thenReturn("ROLE_EMPLOYEE");

        filterWithoutUserDetailsService.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    @DisplayName("Should not overwrite existing SecurityContext authentication")
    void shouldNotOverwriteExistingAuthentication() throws ServletException, IOException {
        Authentication existingAuth = new UsernamePasswordAuthenticationToken(
                "existing.user@example.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        String token = "valid.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn("new.user@example.com");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should safely handle unexpected exceptions during filter processing without aborting chain")
    void shouldHandleUnexpectedExceptionsGracefully() throws ServletException, IOException {
        String token = "problematic.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthenticationFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.validateToken(token)).thenThrow(new RuntimeException("Unexpected parsing failure"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
