package com.leonardoborges.api.util;

import com.leonardoborges.api.exception.BusinessException;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtils Tests")
class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    @DisplayName("Should get current user when authenticated")
    void shouldGetCurrentUser_WhenAuthenticated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = securityUtils.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when not authenticated")
    void shouldThrowException_WhenNotAuthenticated() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUser();
        });
    }

    @Test
    @DisplayName("Should throw exception when authentication is null")
    void shouldThrowException_WhenAuthenticationIsNull() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUser();
        });
    }

    @Test
    @DisplayName("Should throw exception when user not found in repository")
    void shouldThrowException_WhenUserNotFoundInRepository() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUser();
        });
    }

    @Test
    @DisplayName("Should throw exception when principal is not UserDetails")
    void shouldThrowException_WhenPrincipalIsNotUserDetails() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not-user-details");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUser();
        });
    }

    @Test
    @DisplayName("Should get current username when authenticated")
    void shouldGetCurrentUsername_WhenAuthenticated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        String result = securityUtils.getCurrentUsername();

        // Assert
        assertEquals("testuser", result);
    }

    @Test
    @DisplayName("Should throw exception when getting username and not authenticated")
    void shouldThrowException_WhenGettingUsernameAndNotAuthenticated() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUsername();
        });
    }

    @Test
    @DisplayName("Should throw exception when getting username and principal is not UserDetails")
    void shouldThrowException_WhenGettingUsernameAndPrincipalIsNotUserDetails() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not-user-details");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUsername();
        });
    }

    @Test
    @DisplayName("Should throw exception when authentication exists but is not authenticated for getCurrentUser")
    void shouldThrowException_WhenAuthenticationExistsButNotAuthenticated_ForGetCurrentUser() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUser();
        });
    }

    @Test
    @DisplayName("Should throw exception when authentication exists but is not authenticated for getCurrentUsername")
    void shouldThrowException_WhenAuthenticationExistsButNotAuthenticated_ForGetCurrentUsername() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            securityUtils.getCurrentUsername();
        });
    }
}
