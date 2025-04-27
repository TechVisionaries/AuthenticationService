package com.medisync.authservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testSettersAndGetters() {
        UserDTO dto = new UserDTO();
        dto.setEmail("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setRole("ADMIN");
        dto.setPassword("pw");

        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test", dto.getFirstName());
        assertEquals("User", dto.getLastName());
        assertEquals("ADMIN", dto.getRole());
        assertEquals("pw", dto.getPassword());
        assertNotNull(dto.toString());
    }
}
