package com.medisync.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("login@example.com");
        dto.setPassword("pw");
        assertEquals("login@example.com", dto.getEmail());
        assertEquals("pw", dto.getPassword());
        assertNotNull(dto.toString());
    }

    @Test
    void testAllArgsConstructor() {
        LoginDTO dto = new LoginDTO("a@b.com", "pw");
        assertEquals("a@b.com", dto.getEmail());
        assertEquals("pw", dto.getPassword());
    }

    @Test
    void testBuilder() {
        LoginDTO dto = LoginDTO.builder()
                .email("builder@example.com")
                .password("builderpw")
                .build();
        assertEquals("builder@example.com", dto.getEmail());
        assertEquals("builderpw", dto.getPassword());
    }
}
