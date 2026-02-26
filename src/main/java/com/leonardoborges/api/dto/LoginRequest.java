package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request DTO for user login")
public class LoginRequest {
    
    @Schema(description = "Username or email", example = "johndoe")
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @Schema(description = "User password", example = "password123")
    @NotBlank(message = "Password is required")
    private String password;
}
