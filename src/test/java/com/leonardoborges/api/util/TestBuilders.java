package com.leonardoborges.api.util;

import com.leonardoborges.api.dto.AuthRequest;
import com.leonardoborges.api.dto.LoginRequest;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Utility builders to facilitate creation of test objects.
 * Follows the Builder pattern to make tests more readable and maintainable.
 * 
 * Best practices applied:
 * - Static methods for quick creation
 * - Sensible default values
 * - Methods for customization when needed
 */
public class TestBuilders {

    private TestBuilders() {
        // Utility class - should not be instantiated
    }

    // ========== User Builders ==========
    
    public static User.UserBuilder defaultUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .roles(Set.of(User.Role.USER));
    }

    public static User.UserBuilder adminUser() {
        return defaultUser()
                .username("admin")
                .email("admin@example.com")
                .roles(Set.of(User.Role.ADMIN));
    }

    public static User buildDefaultUser() {
        return defaultUser().build();
    }

    public static User buildAdminUser() {
        return adminUser().build();
    }

    // ========== Task Builders ==========
    
    public static Task.TaskBuilder defaultTask() {
        return Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false);
    }

    public static Task buildDefaultTask() {
        return defaultTask().build();
    }

    public static Task buildTaskWithStatus(Task.TaskStatus status) {
        return defaultTask()
                .status(status)
                .build();
    }

    public static Task buildDeletedTask() {
        return defaultTask()
                .deleted(true)
                .deletedBy("testuser")
                .deletedAt(LocalDateTime.now())
                .build();
    }

    // ========== TaskRequest Builders ==========
    
    public static TaskRequest.TaskRequestBuilder defaultTaskRequest() {
        return TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(0L);
    }

    public static TaskRequest buildDefaultTaskRequest() {
        return defaultTaskRequest().build();
    }

    public static TaskRequest buildTaskRequestWithStatus(Task.TaskStatus status) {
        return defaultTaskRequest()
                .status(status)
                .build();
    }

    // ========== TaskResponse Builders ==========
    
    public static TaskResponse.TaskResponseBuilder defaultTaskResponse() {
        return TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static TaskResponse buildDefaultTaskResponse() {
        return defaultTaskResponse().build();
    }

    // ========== AuthRequest Builders ==========
    
    public static AuthRequest buildDefaultAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }

    public static AuthRequest buildAuthRequest(String username, String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // ========== LoginRequest Builders ==========
    
    public static LoginRequest buildDefaultLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");
        return request;
    }

    public static LoginRequest buildLoginRequest(String usernameOrEmail, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(usernameOrEmail);
        request.setPassword(password);
        return request;
    }
}
