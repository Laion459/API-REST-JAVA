package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.AuthRequest;
import com.leonardoborges.api.dto.AuthResponse;
import com.leonardoborges.api.dto.LoginRequest;
import com.leonardoborges.api.dto.RefreshTokenRequest;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.constants.SecurityConstants;
import com.leonardoborges.api.service.JwtService;
import com.leonardoborges.api.service.TokenBlacklistService;
import com.leonardoborges.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and returns a JWT token. Public endpoint, does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or user/email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded (limit: 5 req/min for auth)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token. Accepts username or email. Public endpoint, does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business validation error (e.g., account disabled)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded (limit: 5 req/min for auth)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for: {}", request.getUsernameOrEmail());
        AuthResponse response = userService.login(request.getUsernameOrEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Refreshes the access token using a valid refresh token. Public endpoint, does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or missing refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded (limit: 5 req/min for auth)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");
        AuthResponse response = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Invalidates the current access token by adding it to the blacklist. Requires authentication.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SecurityConstants.BEARER_PREFIX_LENGTH);
            try {
                long expirationTime = jwtService.extractExpiration(token).getTime() - System.currentTimeMillis();
                if (expirationTime > SecurityConstants.MIN_TOKEN_EXPIRATION_TIME_MS) {
                    tokenBlacklistService.blacklistToken(token, expirationTime);
                    log.info("User logged out successfully");
                }
            } catch (Exception e) {
                log.warn("Error during logout: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }
}
