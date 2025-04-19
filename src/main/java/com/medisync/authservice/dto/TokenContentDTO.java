package com.medisync.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Token DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenContentDTO {
    private String token;
}
