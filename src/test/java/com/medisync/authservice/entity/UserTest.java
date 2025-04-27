package com.medisync.authservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("ADMIN");
        user.setPassword("secret");

        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("ADMIN", user.getRole());
        assertEquals("secret", user.getPassword());
        assertNotNull(user.toString());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User(2L, "a@b.com", "A", "B", "USER", "pw");
        assertEquals(2L, user.getId());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("A", user.getFirstName());
        assertEquals("B", user.getLastName());
        assertEquals("USER", user.getRole());
        assertEquals("pw", user.getPassword());
    }
}
