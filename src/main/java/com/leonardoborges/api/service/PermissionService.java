package com.leonardoborges.api.service;

import com.leonardoborges.api.domain.permission.Permission;
import com.leonardoborges.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing granular permissions.
 * Provides fine-grained access control beyond simple roles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    
    /**
     * Checks if the current user has a specific permission.
     * 
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Admin users have all permissions
        if (hasAdminRole(authentication)) {
            return true;
        }
        
        // Check if user has the specific permission
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String permissionString = permission.getValue();
        
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(permissionString) 
                        || authority.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * Checks if the current user has any of the specified permissions.
     * 
     * @param permissions The permissions to check
     * @return true if the user has at least one permission, false otherwise
     */
    public boolean hasAnyPermission(Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the current user has all of the specified permissions.
     * 
     * @param permissions The permissions to check
     * @return true if the user has all permissions, false otherwise
     */
    public boolean hasAllPermissions(Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets all permissions for a user based on their roles.
     * 
     * @param user The user
     * @return Set of permissions
     */
    public Set<Permission> getPermissionsForUser(User user) {
        if (user == null || user.getRoles() == null) {
            return Set.of();
        }
        
        // Admin users have all permissions
        if (user.getRoles().contains(User.Role.ADMIN)) {
            return Set.of(Permission.values());
        }
        
        // Regular users have basic task permissions
        return Set.of(
                Permission.TASK_CREATE,
                Permission.TASK_READ,
                Permission.TASK_UPDATE,
                Permission.TASK_DELETE,
                Permission.TASK_PATCH,
                Permission.TASK_RESTORE
        );
    }
    
    /**
     * Checks if the authentication has admin role.
     */
    private boolean hasAdminRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
