package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO com informações de autenticação e token JWT")
public class AuthResponse {
    
    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Tipo do token", example = "Bearer")
    @Builder.Default
    private String type = "Bearer";
    
    @Schema(description = "ID do usuário", example = "1")
    private Long id;
    
    @Schema(description = "Username do usuário", example = "johndoe")
    private String username;
    
    @Schema(description = "Email do usuário", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Roles do usuário", example = "[\"USER\"]")
    private Set<String> roles;
}
