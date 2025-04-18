package com.medisync.authservice.service;

import com.medisync.authservice.dto.ApiResponseDTO;
import com.medisync.authservice.dto.LoginDTO;
import com.medisync.authservice.dto.UserDTO;
import jakarta.servlet.http.HttpServletResponse;

//userService interface
public interface UserService {
    ApiResponseDTO registerUser(UserDTO userDTO);

    ApiResponseDTO authenticateUser(LoginDTO loginDTO, HttpServletResponse response);
}
