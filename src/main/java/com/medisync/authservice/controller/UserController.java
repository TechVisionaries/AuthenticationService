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
@RequestMapping("/api/authentication")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;
    private static final String SUCCESS_MSG = "Success";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("user/signup")
    public ResponseEntity<ApiResponseDTO> signup(@RequestBody UserDTO userDTO) {
        ApiResponseDTO responseDTO = userService.registerUser(userDTO);
        HttpStatus status = switch (responseDTO.getResponseCode()) {
            case "00" -> HttpStatus.CREATED;
            case "04"-> HttpStatus.ALREADY_REPORTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(responseDTO, status);
    }

    @PostMapping("user/login")
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

    @GetMapping("user-profile/me")
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
                new ApiResponseDTO("00", SUCCESS_MSG, userDTO),
                HttpStatus.OK
        );
    }

    //only access by ADMIN
    @GetMapping("users/all-users")
    //@PreAuthorize("hasRole('ADMIN')") // Optional if handled in SecurityConfig
    public ResponseEntity<ApiResponseDTO> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(
                new ApiResponseDTO("00", SUCCESS_MSG, users),
                HttpStatus.OK
        );
    }

    @GetMapping("")
    //@PreAuthorize("hasRole('ADMIN')") // Optional if handled in SecurityConfig
    public ResponseEntity<ApiResponseDTO> testServer() {
        return new ResponseEntity<>(
                new ApiResponseDTO("00", SUCCESS_MSG,"Server is up and Running"),
                HttpStatus.OK
        );
    }
}
