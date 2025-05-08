package com.medisync.authservice.controller;

import com.medisync.authservice.dto.ApiResponseDTO;
import com.medisync.authservice.dto.LoginDTO;
import com.medisync.authservice.dto.UserDTO;
import com.medisync.authservice.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//user Controller
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDTO> signup(@RequestBody UserDTO userDTO) {
        ApiResponseDTO responseDTO = userService.registerUser(userDTO);
        HttpStatus status = switch (responseDTO.getResponseCode()) {
            case "00" -> HttpStatus.CREATED;
            case "04"-> HttpStatus.ALREADY_REPORTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(responseDTO, status);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO> login(@RequestBody LoginDTO loginDTO, HttpServletResponse httpServletResponse) {
        ApiResponseDTO responseDTO = userService.authenticateUser(loginDTO, httpServletResponse);

        HttpStatus status = switch (responseDTO.getResponseCode()) {
            case "00" -> HttpStatus.CREATED;
            case "02" -> HttpStatus.OK;
            case "03" -> HttpStatus.ALREADY_REPORTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return new ResponseEntity<>(responseDTO, status);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO> getCurrentUser() {
        // Get the email from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (authentication != null) ? authentication.getName() : null;

        if (email == null) {
            return new ResponseEntity<>(
                    new ApiResponseDTO("03", "Unauthorized", null),
                    HttpStatus.FORBIDDEN
            );
        }

        // Fetch user details from the database using the email
        UserDTO userDTO = userService.findByEmail(email);
        if (userDTO == null) {
            return new ResponseEntity<>(
                    new ApiResponseDTO("02", "User not found", null),
                    HttpStatus.NOT_FOUND
            );
        }

        return new ResponseEntity<>(
                new ApiResponseDTO("00", "Success", userDTO),
                HttpStatus.OK
        );
    }

    //only access by ADMIN
    @GetMapping("/all-users")
    //@PreAuthorize("hasRole('ADMIN')") // Optional if handled in SecurityConfig
    public ResponseEntity<ApiResponseDTO> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(
                new ApiResponseDTO("00", "Success", users),
                HttpStatus.OK
        );
    }
}
