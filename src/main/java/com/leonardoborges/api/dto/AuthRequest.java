package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.leonardoborges.api.constants.SecurityConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request DTO for new user registration")
public class AuthRequest {
    
    @Schema(description = "Unique username", example = "johndoe", minLength = 3, maxLength = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Schema(description = "User email", example = "john.doe@example.com", maxLength = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Schema(description = "User password", example = "Password123", 
            minLength = SecurityConstants.MIN_PASSWORD_LENGTH, 
            maxLength = SecurityConstants.MAX_PASSWORD_LENGTH)
    @NotBlank(message = "Password is required")
    @Size(min = SecurityConstants.MIN_PASSWORD_LENGTH, 
          max = SecurityConstants.MAX_PASSWORD_LENGTH, 
          message = "Password must be between " + SecurityConstants.MIN_PASSWORD_LENGTH + 
                    " and " + SecurityConstants.MAX_PASSWORD_LENGTH + " characters")
    private String password;
}
