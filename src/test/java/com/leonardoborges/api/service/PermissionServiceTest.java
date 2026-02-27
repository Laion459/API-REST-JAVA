package com.leonardoborges.api.service;

import com.leonardoborges.api.domain.permission.Permission;
import com.leonardoborges.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Permission Service Tests")
class PermissionServiceTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private GrantedAuthority authority;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should return permissions for admin user")
    void shouldReturnPermissionsForAdminUser() {
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .roles(Set.of(User.Role.ADMIN))
                .build();

        Set<Permission> permissions = permissionService.getPermissionsForUser(adminUser);
        assertEquals(Set.of(Permission.values()), permissions);
    }

    @Test
    @DisplayName("Should return basic permissions for regular user")
    void shouldReturnBasicPermissionsForRegularUser() {
        User regularUser = User.builder()
                .id(1L)
                .username("user")
                .roles(Set.of(User.Role.USER))
                .build();

        Set<Permission> permissions = permissionService.getPermissionsForUser(regularUser);
        assertTrue(permissions.contains(Permission.TASK_CREATE));
        assertTrue(permissions.contains(Permission.TASK_READ));
        assertFalse(permissions.contains(Permission.ADMIN_ACCESS));
    }

    @Test
    @DisplayName("Should return empty set for null user")
    void shouldReturnEmptySetForNullUser() {
        Set<Permission> permissions = permissionService.getPermissionsForUser(null);
        assertTrue(permissions.isEmpty());
    }

    @Test
    @DisplayName("Should check permission correctly")
    void shouldCheckPermissionCorrectly() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection) Set.of(authority));
        when(authority.getAuthority()).thenReturn("ROLE_ADMIN");

        assertTrue(permissionService.hasPermission(Permission.TASK_CREATE));
    }

    @Test
    @DisplayName("Should return false for unauthenticated user")
    void shouldReturnFalseForUnauthenticatedUser() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertFalse(permissionService.hasPermission(Permission.TASK_CREATE));
    }
}
