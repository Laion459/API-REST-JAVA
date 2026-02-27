package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.AuthRequest;
import com.leonardoborges.api.dto.AuthResponse;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.model.RefreshToken;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.UserRepository;
import com.leonardoborges.api.service.interfaces.IUserService;
import com.leonardoborges.api.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final InputSanitizer inputSanitizer;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = findUserByUsernameOrEmail(username);
        
        if (!user.getEnabled()) {
            throw new BusinessException("User account is disabled");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream()
                        .map(role -> role.name())
                        .toArray(String[]::new))
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail)));
    }

    @Override
    @Transactional
    public AuthResponse register(AuthRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Sanitize inputs
        String username = inputSanitizer.sanitizeString(request.getUsername());
        String email = inputSanitizer.sanitizeString(request.getEmail()).toLowerCase();

        // Validate uniqueness
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(User.Role.USER))
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
        
        // Audit sensitive operation
        auditService.audit("USER_REGISTERED", "User", user.getId(), 
                String.format("Username: %s, Email: %s", user.getUsername(), user.getEmail()));

        // Generate tokens
        UserDetails userDetails = loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        
        // Audit authentication
        auditService.auditAuthentication("REGISTRATION_SUCCESS", user.getUsername(), "New user registered");

        return buildAuthResponse(user, token, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(String usernameOrEmail, String password) {
        log.info("Login attempt for: {}", usernameOrEmail);

        UserDetails userDetails = loadUserByUsername(usernameOrEmail);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("Invalid password for user: {}", usernameOrEmail);
            auditService.auditAuthentication("LOGIN_FAILED", usernameOrEmail, "Invalid password");
            throw new BusinessException("Invalid credentials");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);

        String token = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("User logged in successfully: {}", user.getUsername());
        
        // Audit successful login
        auditService.auditAuthentication("LOGIN_SUCCESS", user.getUsername(), "User authenticated successfully");

        return buildAuthResponse(user, token, refreshToken);
    }
    
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        log.info("Refresh token request");
        
        // Validate and use refresh token (marks as used)
        RefreshToken refreshToken = refreshTokenService.validateAndUseToken(refreshTokenString);
        User user = refreshToken.getUser();
        
        // Load user and generate new tokens
        UserDetails userDetails = loadUserByUsername(user.getUsername());
        String newToken = jwtService.generateToken(userDetails);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);
        
        log.info("Tokens refreshed successfully for user: {}", user.getUsername());
        
        // Audit token refresh
        auditService.auditAuthentication("TOKEN_REFRESHED", user.getUsername(), "Access token renewed");
        
        return buildAuthResponse(user, newToken, newRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.name())
                        .collect(Collectors.toSet()))
                .build();
    }
}
