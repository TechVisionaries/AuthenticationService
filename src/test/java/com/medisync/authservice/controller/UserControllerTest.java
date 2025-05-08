package com.medisync.authservice.controller;

import com.medisync.authservice.dto.ApiResponseDTO;
import com.medisync.authservice.dto.LoginDTO;
import com.medisync.authservice.dto.UserDTO;
import com.medisync.authservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private HttpServletResponse httpServletResponse;
    @InjectMocks private UserController userController;

    private UserDTO validUserDTO;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setUp() {
        validUserDTO = new UserDTO();
        validUserDTO.setEmail("test@example.com");
        validUserDTO.setFirstName("Test");
        validUserDTO.setLastName("User");
        validUserDTO.setPassword("password");
        validUserDTO.setRole("USER");

        validLoginDTO = new LoginDTO();
        validLoginDTO.setEmail("test@example.com");
        validLoginDTO.setPassword("password");
    }

    @Test
    void signup_withValidUser_shouldReturnCreated() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("00", "Success", null);
        when(userService.registerUser(any(UserDTO.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.signup(validUserDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("00", response.getBody().getResponseCode());
    }

    @Test
    void signup_withExistingUser_shouldReturnAlreadyReported() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("04", "User Already Exists", null);
        when(userService.registerUser(any(UserDTO.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.signup(validUserDTO);

        assertEquals(HttpStatus.ALREADY_REPORTED, response.getStatusCode());
        assertEquals("04", response.getBody().getResponseCode());
    }

    @Test
    void signup_withUnknownResponseCode_shouldReturnInternalServerError() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("99", "Unknown Error", null); // "99" is not handled
        when(userService.registerUser(any(UserDTO.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.signup(validUserDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getResponseCode());
    }

    @Test
    void login_withCorrectCredentials_shouldReturnCreated() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("00", "Success", null);
        when(userService.authenticateUser(any(LoginDTO.class), any(HttpServletResponse.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.login(validLoginDTO, httpServletResponse);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("00", response.getBody().getResponseCode());
    }

    @Test
    void login_withWrongCredentials_shouldReturnAlreadyReported() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("03", "Invalid Credentials", null);
        when(userService.authenticateUser(any(LoginDTO.class), any(HttpServletResponse.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.login(validLoginDTO, httpServletResponse);

        assertEquals(HttpStatus.ALREADY_REPORTED, response.getStatusCode());
        assertEquals("03", response.getBody().getResponseCode());
    }

    @Test
    void login_withNonExistentUser_shouldReturnOk() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("02", "No such user exists!", null);
        when(userService.authenticateUser(any(LoginDTO.class), any(HttpServletResponse.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.login(validLoginDTO, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("02", response.getBody().getResponseCode());
    }

    @Test
    void login_withUnknownResponseCode_shouldReturnInternalServerError() {
        ApiResponseDTO responseDTO = new ApiResponseDTO("99", "Unknown Error", null); // "99" is not handled
        when(userService.authenticateUser(any(LoginDTO.class), any(HttpServletResponse.class))).thenReturn(responseDTO);

        ResponseEntity<ApiResponseDTO> response = userController.login(validLoginDTO, httpServletResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getResponseCode());
    }


    @Test
    void getCurrentUser_whenAuthenticated_shouldReturnSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(userDTO);

        // Mock SecurityContextHolder
        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            var context = mock(org.springframework.security.core.context.SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(org.springframework.security.core.context.SecurityContextHolder::getContext).thenReturn(context);

            ResponseEntity<ApiResponseDTO> response = userController.getCurrentUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("00", response.getBody().getResponseCode());
            assertNotNull(response.getBody().getContent());
        }
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_shouldReturnForbidden() {
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            var context = mock(org.springframework.security.core.context.SecurityContext.class);
            when(context.getAuthentication()).thenReturn(null);
            mocked.when(org.springframework.security.core.context.SecurityContextHolder::getContext).thenReturn(context);

            ResponseEntity<ApiResponseDTO> response = userController.getCurrentUser();

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertEquals("03", response.getBody().getResponseCode());
        }
    }

    @Test
    void getCurrentUser_whenUserNotFound_shouldReturnNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(null);
        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            var context = mock(org.springframework.security.core.context.SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(org.springframework.security.core.context.SecurityContextHolder::getContext).thenReturn(context);

            ResponseEntity<ApiResponseDTO> response = userController.getCurrentUser();

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("02", response.getBody().getResponseCode());
        }
    }

    @Test
    void getAllUsers_shouldReturnSuccess() {
        UserDTO user1 = new UserDTO();
        user1.setEmail("user1@example.com");
        UserDTO user2 = new UserDTO();
        user2.setEmail("user2@example.com");
        List<UserDTO> users = List.of(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<ApiResponseDTO> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("00", response.getBody().getResponseCode());
        assertEquals(2, ((List<?>) response.getBody().getContent()).size());
    }
}