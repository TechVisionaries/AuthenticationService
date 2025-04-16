package com.medisync.authservice.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String password;
}
