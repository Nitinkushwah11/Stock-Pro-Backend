package com.stockpro.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Email
    private String email;

    @NotBlank
    private String password;
}