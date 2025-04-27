package com.medisync.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenContentDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        TokenContentDTO dto = new TokenContentDTO();
        dto.setToken("tok");
        assertEquals("tok", dto.getToken());
        assertNotNull(dto.toString());
    }

    @Test
    void testAllArgsConstructor() {
        TokenContentDTO dto = new TokenContentDTO("tokenValue");
        assertEquals("tokenValue", dto.getToken());
    }
}
