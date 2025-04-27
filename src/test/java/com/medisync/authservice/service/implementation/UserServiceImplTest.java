package com.medisync.authservice.service.implementation;

import com.medisync.authservice.dto.ApiResponseDTO;
import com.medisync.authservice.dto.LoginDTO;
import com.medisync.authservice.dto.UserDTO;
import com.medisync.authservice.entity.User;
import com.medisync.authservice.repository.UserRepository;
import com.medisync.authservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private HttpServletResponse httpServletResponse;
    @InjectMocks private UserServiceImpl userService;

    private UserDTO userDTO;
    private LoginDTO loginDTO;
    private User userEntity;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setFirstName("Test");
        userDTO.setLastName("User");
        userDTO.setPassword("password");
        userDTO.setRole("USER");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password");

        userEntity = new User();
        userEntity.setEmail("test@example.com");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setPassword("encodedPassword");
        userEntity.setRole("USER");
    }

    @Test
    void registerUser_newUser_shouldReturnSuccess() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);

        ApiResponseDTO response = userService.registerUser(userDTO);

        assertEquals("00", response.getResponseCode());
        assertEquals("Success", response.getResponseMsg());
    }

    @Test
    void registerUser_existingUser_shouldReturnUserAlreadyExists() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(userEntity));

        ApiResponseDTO response = userService.registerUser(userDTO);

        assertEquals("04", response.getResponseCode());
        assertEquals("User Already Exists", response.getResponseMsg());
    }

    @Test
    void registerUser_exception_shouldReturnBadRequest() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenThrow(new RuntimeException("DB error"));

        ApiResponseDTO response = userService.registerUser(userDTO);

        assertEquals("06", response.getResponseCode());
        assertEquals("Bad Request", response.getResponseMsg());
    }

    @Test
    void authenticateUser_validCredentials_shouldReturnSuccess() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginDTO.getPassword(), userEntity.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(userEntity.getEmail(), userEntity.getRole())).thenReturn("token");

        ApiResponseDTO response = userService.authenticateUser(loginDTO, httpServletResponse);

        assertEquals("00", response.getResponseCode());
        assertEquals("Success", response.getResponseMsg());
        assertNotNull(response.getContent());
    }

    @Test
    void authenticateUser_invalidCredentials_shouldReturnInvalidCredentials() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(loginDTO.getPassword(), userEntity.getPassword())).thenReturn(false);

        ApiResponseDTO response = userService.authenticateUser(loginDTO, httpServletResponse);

        assertEquals("03", response.getResponseCode());
        assertEquals("Invalid Credentials", response.getResponseMsg());
    }

    @Test
    void authenticateUser_nonExistentUser_shouldReturnNoSuchUser() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        ApiResponseDTO response = userService.authenticateUser(loginDTO, httpServletResponse);

        assertEquals("02", response.getResponseCode());
        assertEquals("No such user exists!", response.getResponseMsg());
    }

    @Test
    void authenticateUser_exception_shouldReturnBadRequest() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenThrow(new RuntimeException("DB error"));

        ApiResponseDTO response = userService.authenticateUser(loginDTO, httpServletResponse);

        assertEquals("06", response.getResponseCode());
        assertEquals("Bad Request", response.getResponseMsg());
    }

    @Test
    void findByEmail_userExists_shouldReturnUserDTO() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(userEntity));

        UserDTO result = userService.findByEmail(userDTO.getEmail());

        assertNotNull(result);
        assertEquals(userDTO.getEmail(), result.getEmail());
    }

    @Test
    void findByEmail_userDoesNotExist_shouldReturnNull() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());

        UserDTO result = userService.findByEmail(userDTO.getEmail());

        assertNull(result);
    }

    @Test
    void getAllUsers_shouldReturnUserDTOList() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(userEntity.getEmail(), result.get(0).getEmail());
    }

    @Test
    void getAllUsers_emptyList_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
