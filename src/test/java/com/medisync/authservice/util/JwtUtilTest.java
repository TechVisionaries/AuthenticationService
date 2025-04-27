package com.medisync.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set secret key (Base64 encoded) for testing
        String secretKey = "ZmFrZXNlY3JldGtleWZha2VzZWNyZXRrZXlmbGFrZXNlY3JldA=="; // base64 for 'fakesecretkeyfakesecretkeyflaksecret'
        ReflectionTestUtils.setField(jwtUtil, "secret", secretKey);
    }

    @Test
    void generateToken_shouldContainEmailAndRole() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode("ZmFrZXNlY3JldGtleWZha2VzZWNyZXRrZXlmbGFrZXNlY3JldA==")))
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void extractRole_shouldReturnCorrectRole() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        String extractedRole = jwtUtil.extractRole(token);
        assertEquals(role, extractedRole);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        assertTrue(jwtUtil.isTokenValid(token, email, role));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidEmail() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        assertFalse(jwtUtil.isTokenValid(token, "wrong@example.com", role));
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidRole() {
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        assertFalse(jwtUtil.isTokenValid(token, email, "ADMIN"));
    }
}
