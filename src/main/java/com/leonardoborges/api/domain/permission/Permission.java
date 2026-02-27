package com.leonardoborges.api.domain.permission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing granular permissions in the system.
 * Provides fine-grained access control beyond simple roles.
 */
@Getter
@RequiredArgsConstructor
public enum Permission {
    
    // Task permissions
    TASK_CREATE("task:create"),
    TASK_READ("task:read"),
    TASK_UPDATE("task:update"),
    TASK_DELETE("task:delete"),
    TASK_PATCH("task:patch"),
    TASK_RESTORE("task:restore"),
    TASK_VIEW_ALL("task:view_all"),
    
    // User permissions
    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    USER_VIEW_ALL("user:view_all"),
    
    // Cache permissions
    CACHE_CLEAR("cache:clear"),
    CACHE_VIEW("cache:view"),
    
    // Admin permissions
    ADMIN_ACCESS("admin:access"),
    ADMIN_AUDIT_VIEW("admin:audit_view"),
    ADMIN_METRICS_VIEW("admin:metrics_view");
    
    private final String value;
    
    /**
     * Checks if a permission string matches this permission.
     */
    public boolean matches(String permissionString) {
        return this.value.equals(permissionString);
    }
    
    /**
     * Gets permission by string value.
     */
    public static Permission fromString(String value) {
        for (Permission permission : values()) {
            if (permission.value.equals(value)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + value);
    }
}
