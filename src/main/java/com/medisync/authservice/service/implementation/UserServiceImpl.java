package com.medisync.authservice.service.implementation;

import com.medisync.authservice.dto.ApiResponseDTO;
import com.medisync.authservice.dto.LoginDTO;
import com.medisync.authservice.dto.TokenContentDTO;
import com.medisync.authservice.dto.UserDTO;
import com.medisync.authservice.entity.User;
import com.medisync.authservice.repository.UserRepository;
import com.medisync.authservice.service.UserService;
import com.medisync.authservice.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


//service implementation
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    AuthenticationManager authenticationManager;

    public ApiResponseDTO registerUser(UserDTO userDTO) {
        log.info("Attempting to register user with email: {}", userDTO.getEmail());
        try {
            // Check if user is already in database
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                log.warn("User already existing in the system: {}", userDTO.getEmail());
                return new ApiResponseDTO("04", "User Already Exists", null);
            }
            User user = new User();
            user.setEmail(userDTO.getEmail());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setRole(userDTO.getRole());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userRepository.save(user);
            log.info("New user added to the system with email : {}", userDTO.getEmail());
            return new ApiResponseDTO("00", "Success", null);
        }catch (Exception e) {
            log.error("Exception occur signup: {}", e.getMessage());
            e.printStackTrace();
            return new ApiResponseDTO("06", "Bad Request", null);
        }
    }

    public ApiResponseDTO authenticateUser(LoginDTO loginDTO, HttpServletResponse response) {
        log.info("Attempting to Log in user with email: {}", loginDTO.getEmail());
        try {
            // Find user by email
            User user = userRepository.findByEmail(loginDTO.getEmail()).orElse(null);
            if (user == null) {
                log.warn("User doesn't exists with email : {}", loginDTO.getEmail());
                return new ApiResponseDTO("02", "No such user exists!", null);
            }

            // Check if authentication was successful
            if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                TokenContentDTO content = new TokenContentDTO(token);
                log.info("User logging successful and content : {}", content);

                // Create a cookie to store the token
                Cookie jwtCookie = new Cookie("jwtToken", token);
                jwtCookie.setHttpOnly(true);    // Makes cookie inaccessible to JavaScript for security
                jwtCookie.setSecure(true);  // Ensures cookie is sent only over HTTPS
                jwtCookie.setPath("/"); // Cookie is accessible across the application
                jwtCookie.setMaxAge(24*60*60);  // Set cookie expiration (1 day)

                // Add the cookie to the response
                response.addCookie(jwtCookie);

                log.info("cookie set successfully");

                return new ApiResponseDTO("00", "Success", content);
            } else {
                log.warn("Invalid credentials provided for email  : {}", loginDTO.getEmail());
                return new ApiResponseDTO("03", "Invalid Credentials", null);
            }
        } catch (Exception e) {
            log.error("Exception occur in logging: {}", e.getMessage());
            e.printStackTrace();
            return new ApiResponseDTO("06", "Bad Request", null);
        }
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setRole(user.getRole());
            return dto;
        }).toList();
    }
}
