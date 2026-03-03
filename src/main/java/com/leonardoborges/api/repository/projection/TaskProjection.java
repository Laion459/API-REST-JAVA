package com.leonardoborges.api.repository.projection;

import com.leonardoborges.api.model.Task;

/**
 * Projection interface for optimized task queries.
 * Reduces data transfer by selecting only necessary fields.
 * Prevents N+1 queries and improves performance.
 */
public interface TaskProjection {
    
    Long getId();
    String getTitle();
    String getDescription();
    Task.TaskStatus getStatus();
    Integer getPriority();
    Long getVersion();
    java.time.LocalDateTime getCreatedAt();
    java.time.LocalDateTime getUpdatedAt();
    
    /**
     * Nested projection for user information.
     * Avoids loading full User entity.
     */
    interface UserInfo {
        Long getId();
        String getUsername();
        String getEmail();
    }
}
