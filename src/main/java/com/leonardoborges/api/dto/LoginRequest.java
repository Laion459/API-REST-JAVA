package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request DTO para login de usuário")
public class LoginRequest {
    
    @Schema(description = "Username ou email do usuário", example = "johndoe")
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @Schema(description = "Senha do usuário", example = "password123")
    @NotBlank(message = "Password is required")
    private String password;
}
