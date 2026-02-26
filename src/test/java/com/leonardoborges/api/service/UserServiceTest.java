package com.leonardoborges.api.service;

import com.leonardoborges.api.audit.AuditService;
import com.leonardoborges.api.dto.AuthRequest;
import com.leonardoborges.api.dto.AuthResponse;
import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.exception.ValidationException;
import com.leonardoborges.api.model.RefreshToken;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.UserRepository;
import com.leonardoborges.api.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditService auditService;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private UserService userService;

    private User user;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .roles(Set.of(User.Role.USER))
                .build();

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should load user by email when not found by username")
    void shouldLoadUserByEmailWhenNotFoundByUsername() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user is not found")
    void shouldThrowExceptionWhenUserIsNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    @DisplayName("Should throw exception when user is disabled")
    void shouldThrowExceptionWhenUserIsDisabled() {
        user.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> {
            userService.loadUserByUsername("testuser");
        });
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        when(inputSanitizer.sanitizeString("testuser")).thenReturn("testuser");
        when(inputSanitizer.sanitizeString("test@example.com")).thenReturn("test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        // Mock loadUserByUsername after user is saved
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = userService.register(authRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
        verify(auditService).audit(anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Should convert email to lowercase during registration")
    void shouldConvertEmailToLowerCase_DuringRegistration() {
        authRequest.setEmail("TEST@EXAMPLE.COM");
        when(inputSanitizer.sanitizeString("testuser")).thenReturn("testuser");
        when(inputSanitizer.sanitizeString("TEST@EXAMPLE.COM")).thenReturn("TEST@EXAMPLE.COM");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(argThat(user -> "test@example.com".equals(user.getEmail())))).thenReturn(user);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = userService.register(authRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        when(inputSanitizer.sanitizeString("testuser")).thenReturn("testuser");
        when(inputSanitizer.sanitizeString("test@example.com")).thenReturn("test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            userService.register(authRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(inputSanitizer.sanitizeString("testuser")).thenReturn("testuser");
        when(inputSanitizer.sanitizeString("test@example.com")).thenReturn("test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            userService.register(authRequest);
        });
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = userService.login("testuser", "password123");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(auditService).auditAuthentication(eq("LOGIN_SUCCESS"), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongPassword");
        });

        verify(auditService).auditAuthentication(eq("LOGIN_FAILED"), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("old-refresh-token")
                .user(user)
                .build();

        when(refreshTokenService.validateAndUseToken("old-refresh-token")).thenReturn(refreshToken);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("new-refresh-token");

        AuthResponse response = userService.refreshToken("old-refresh-token");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(refreshTokenService).validateAndUseToken("old-refresh-token");
        verify(auditService).auditAuthentication(eq("TOKEN_REFRESHED"), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("Should find user by username or email")
    void shouldFindUserByUsernameOrEmail() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User found = userService.findUserByUsernameOrEmail("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
    }

    @Test
    @DisplayName("Should find user by email when not found by username")
    void shouldFindUserByEmailWhenNotFoundByUsername() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User found = userService.findUserByUsernameOrEmail("test@example.com");

        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found by username or email")
    void shouldThrowException_WhenUserNotFoundByUsernameOrEmail() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findUserByUsernameOrEmail("nonexistent");
        });
    }

    @Test
    @DisplayName("Should handle empty email sanitization during registration")
    void shouldHandleEmptyEmailSanitization_DuringRegistration() {
        authRequest.setEmail("   ");
        when(inputSanitizer.sanitizeString("testuser")).thenReturn("testuser");
        when(inputSanitizer.sanitizeString("   ")).thenReturn("");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = userService.register(authRequest);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle login with email instead of username")
    void shouldHandleLogin_WithEmailInsteadOfUsername() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = userService.login("test@example.com", "password123");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository).findByEmail("test@example.com");
        verify(auditService).auditAuthentication(eq("LOGIN_SUCCESS"), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("Should handle disabled user when loading by email")
    void shouldHandleDisabledUser_WhenLoadingByEmail() {
        user.setEnabled(false);
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> {
            userService.loadUserByUsername("test@example.com");
        });
    }

    @Test
    @DisplayName("Should handle user with multiple roles")
    void shouldHandleUser_WithMultipleRoles() {
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .password("encodedPassword")
                .enabled(true)
                .roles(Set.of(User.Role.USER, User.Role.ADMIN))
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        UserDetails userDetails = userService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
