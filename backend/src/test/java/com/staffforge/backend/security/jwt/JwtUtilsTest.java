package com.staffforge.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    // 256-bit Base64-encoded secret key for testing
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D6351";
    private static final String DIFFERENT_SECRET = "6150645367566B5970337336763979244226452948404D6351404E635266556A586E3272357538782F413F4428472B4B";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private static final String USERNAME = "john.doe";
    private static final String ROLE = "ROLE_PROJECT_MANAGER";

    private JwtUtils jwtUtils;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(TEST_SECRET, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should successfully generate a valid JWT token given username and role")
        void shouldGenerateTokenGivenValidUsernameAndRole() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            assertNotNull(token);
            assertFalse(token.isBlank());

            // A compact JWT consists of three parts separated by dots (header.payload.signature)
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "Token must contain header, payload, and signature components");
        }

        @Test
        @DisplayName("Should generate token using Spring Security UserDetails mocked via Mockito")
        void shouldGenerateTokenUsingUserDetails_withMockito() {
            when(userDetails.getUsername()).thenReturn(USERNAME);
            GrantedAuthority authority = () -> ROLE;
            doReturn(Collections.singleton(authority)).when(userDetails).getAuthorities();

            String token = jwtUtils.generateToken(userDetails);

            assertNotNull(token);
            assertEquals(USERNAME, jwtUtils.extractUsername(token));
            assertEquals(ROLE, jwtUtils.extractRole(token));
            assertTrue(jwtUtils.isTokenValid(token, userDetails));
            verify(userDetails, atLeastOnce()).getUsername();
        }

        @Test
        @DisplayName("Should generate token with custom extra claims")
        void shouldGenerateTokenWithExtraClaims() {
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", ROLE);
            extraClaims.put("department", "Engineering");
            extraClaims.put("userId", 42L);

            String token = jwtUtils.generateToken(USERNAME, extraClaims);

            assertNotNull(token);
            assertEquals(USERNAME, jwtUtils.extractUsername(token));
            assertEquals("Engineering", jwtUtils.extractClaim(token, claims -> claims.get("department", String.class)));
            assertEquals(42L, (Long) jwtUtils.extractClaim(token, claims -> claims.get("userId", Long.class)));
        }
    }

    @Nested
    @DisplayName("Claim Extraction Tests")
    class ClaimExtractionTests {

        @Test
        @DisplayName("Should correctly extract username from a generated token")
        void shouldExtractUsernameCorrectly() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            String extractedUsername = jwtUtils.extractUsername(token);

            assertEquals(USERNAME, extractedUsername);
        }

        @Test
        @DisplayName("Should correctly extract role from a generated token")
        void shouldExtractRoleCorrectly() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            String extractedRole = jwtUtils.extractRole(token);

            assertEquals(ROLE, extractedRole);
        }

        @Test
        @DisplayName("Should correctly extract expiration date in the future")
        void shouldExtractExpirationCorrectly() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            Date expiration = jwtUtils.extractExpiration(token);

            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()), "Token expiration must be in the future");
        }

        @Test
        @DisplayName("Should extract all claims successfully for a valid token")
        void shouldExtractAllClaimsSuccessfully() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            Claims claims = jwtUtils.extractAllClaims(token);

            assertNotNull(claims);
            assertEquals(USERNAME, claims.getSubject());
            assertEquals(ROLE, claims.get(JwtUtils.CLAIM_ROLE, String.class));
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiration());
        }
    }

    @Nested
    @DisplayName("Token Rejection & Negative Test Cases (DoD: Expired or Altered Tokens)")
    class TokenRejectionTests {

        @Test
        @DisplayName("Should identify and reject expired token (validateToken returns false, isTokenExpired returns true)")
        void shouldRejectExpiredToken() {
            // Generate a token that expired 10 seconds ago
            String expiredToken = jwtUtils.generateToken(USERNAME, ROLE, -10000);

            // Validation must fail
            assertFalse(jwtUtils.validateToken(expiredToken), "validateToken must return false for expired token");
            assertTrue(jwtUtils.isTokenExpired(expiredToken), "isTokenExpired must return true for expired token");
            assertFalse(jwtUtils.isTokenValid(expiredToken, USERNAME), "isTokenValid must return false for expired token");
        }

        @Test
        @DisplayName("Should throw ExpiredJwtException when attempting to extract claims from expired token")
        void shouldThrowExceptionWhenExtractingClaimsFromExpiredToken() {
            String expiredToken = jwtUtils.generateToken(USERNAME, ROLE, -5000);

            ExpiredJwtException exception = assertThrows(
                    ExpiredJwtException.class,
                    () -> jwtUtils.extractAllClaims(expiredToken),
                    "extractAllClaims must throw ExpiredJwtException on expired token"
            );

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should identify and reject maliciously altered token (tampered payload)")
        void shouldRejectMaliciouslyAlteredPayload() {
            String validToken = jwtUtils.generateToken(USERNAME, ROLE);
            String[] parts = validToken.split("\\.");

            // Tamper with payload (middle component)
            // Replace a character in payload base64 string
            char originalChar = parts[1].charAt(10);
            char alteredChar = (originalChar == 'a') ? 'b' : 'a';
            String alteredPayload = parts[1].substring(0, 10) + alteredChar + parts[1].substring(11);
            String alteredToken = parts[0] + "." + alteredPayload + "." + parts[2];

            assertFalse(jwtUtils.validateToken(alteredToken), "validateToken must reject altered token");
            assertFalse(jwtUtils.isTokenValid(alteredToken, USERNAME), "isTokenValid must reject altered token");
            assertThrows(
                    SignatureException.class,
                    () -> jwtUtils.extractAllClaims(alteredToken),
                    "extractAllClaims must throw SignatureException on altered token payload"
            );
        }

        @Test
        @DisplayName("Should identify and reject maliciously altered token (tampered signature)")
        void shouldRejectMaliciouslyAlteredSignature() {
            String validToken = jwtUtils.generateToken(USERNAME, ROLE);
            String[] parts = validToken.split("\\.");

            // Alter the signature by replacing the last 5 characters
            String alteredSignature = parts[2].substring(0, parts[2].length() - 5) + "XXXXX";
            String alteredToken = parts[0] + "." + parts[1] + "." + alteredSignature;

            assertFalse(jwtUtils.validateToken(alteredToken), "validateToken must reject tampered signature");
            assertFalse(jwtUtils.isTokenValid(alteredToken, USERNAME), "isTokenValid must reject tampered signature");
            assertThrows(
                    SignatureException.class,
                    () -> jwtUtils.extractAllClaims(alteredToken),
                    "extractAllClaims must throw SignatureException on tampered signature"
            );
        }

        @Test
        @DisplayName("Should reject token signed with a completely different secret key")
        void shouldRejectTokenSignedWithDifferentSecretKey() {
            // Build token signed with a different key
            SecretKey diffKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(DIFFERENT_SECRET));
            String forgedToken = Jwts.builder()
                    .subject(USERNAME)
                    .claim(JwtUtils.CLAIM_ROLE, ROLE)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(diffKey)
                    .compact();

            assertFalse(jwtUtils.validateToken(forgedToken), "Token signed with different key must be rejected");
            assertFalse(jwtUtils.isTokenValid(forgedToken, USERNAME));
            assertThrows(SignatureException.class, () -> jwtUtils.extractAllClaims(forgedToken));
        }

        @ParameterizedTest(name = "Should reject null or empty token: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void shouldRejectNullOrEmptyTokens(String emptyToken) {
            assertFalse(jwtUtils.validateToken(emptyToken));
            assertFalse(jwtUtils.isTokenValid(emptyToken, USERNAME));
        }

        @Test
        @DisplayName("Should reject malformed JWT strings")
        void shouldRejectMalformedToken() {
            String malformedToken = "not.a.valid.jwt.token";

            assertFalse(jwtUtils.validateToken(malformedToken));
            assertThrows(JwtException.class, () -> jwtUtils.extractAllClaims(malformedToken));
        }

        @Test
        @DisplayName("Should reject token when username does not match expected username")
        void shouldRejectTokenWhenUsernameDoesNotMatch() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            assertFalse(jwtUtils.isTokenValid(token, "wrong.user"));
            assertFalse(jwtUtils.isTokenValid(token, (String) null));
        }
    }

    @Nested
    @DisplayName("Token Validation Success Tests")
    class TokenValidationSuccessTests {

        @Test
        @DisplayName("Should validate genuine, unexpired token successfully")
        void shouldValidateGenuineTokenSuccessfully() {
            String token = jwtUtils.generateToken(USERNAME, ROLE);

            assertTrue(jwtUtils.validateToken(token));
            assertTrue(jwtUtils.isTokenValid(token, USERNAME));
            assertFalse(jwtUtils.isTokenExpired(token));
            assertDoesNotThrow(() -> jwtUtils.extractAllClaims(token));
        }
    }
}
