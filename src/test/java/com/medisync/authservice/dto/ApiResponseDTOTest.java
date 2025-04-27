package com.medisync.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ApiResponseDTO dto = new ApiResponseDTO();
        dto.setResponseCode("00");
        dto.setResponseMsg("Success");
        dto.setContent("data");

        assertEquals("00", dto.getResponseCode());
        assertEquals("Success", dto.getResponseMsg());
        assertEquals("data", dto.getContent());
        assertNotNull(dto.toString());
    }

    @Test
    void testAllArgsConstructor() {
        ApiResponseDTO dto = new ApiResponseDTO("01", "Fail", null);
        assertEquals("01", dto.getResponseCode());
        assertEquals("Fail", dto.getResponseMsg());
        assertNull(dto.getContent());
    }

    @Test
    void testBuilder() {
        ApiResponseDTO dto = ApiResponseDTO.builder()
                .responseCode("02")
                .responseMsg("Builder")
                .content("builderData")
                .build();
        assertEquals("02", dto.getResponseCode());
        assertEquals("Builder", dto.getResponseMsg());
        assertEquals("builderData", dto.getContent());
    }
}

