package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskPageResponse;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.service.ReactiveTaskService;
import com.leonardoborges.api.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@WebFluxTest(controllers = ReactiveTaskController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.r2dbc.enabled=true"})
@DisplayName("ReactiveTaskController Tests")
class ReactiveTaskControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveTaskService reactiveTaskService;

    @MockBean
    private SecurityUtils securityUtils;

    private User currentUser;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of(User.Role.USER))
                .build();

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("Should get task by ID successfully")
    void shouldGetTaskByIdSuccessfully() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(reactiveTaskService.getTaskById(1L, 1L)).thenReturn(Mono.just(taskResponse));

        webTestClient.get()
                .uri("/api/v2/reactive/tasks/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(response -> {
                    assert response.getId().equals(1L);
                    assert response.getTitle().equals("Test Task");
                });
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(reactiveTaskService.getTaskById(999L, 1L)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v2/reactive/tasks/{id}", 999L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @WithMockUser
    @DisplayName("Should get all tasks with pagination")
    void shouldGetAllTasksWithPagination() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        List<TaskResponse> tasks = List.of(taskResponse);
        when(reactiveTaskService.getAllTasks(0, 20, 1L))
                .thenReturn(Flux.fromIterable(tasks));

        webTestClient.get()
                .uri("/api/v2/reactive/tasks?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskPageResponse.class)
                .value(response -> {
                    assert response.getContent().size() == 1;
                    assert response.getNumber() == 0;
                    assert response.getSize() == 20;
                    assert Boolean.TRUE.equals(response.getFirst());
                });
    }

    @Test
    @WithMockUser
    @DisplayName("Should get tasks by status")
    void shouldGetTasksByStatus() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        List<TaskResponse> tasks = List.of(taskResponse);
        when(reactiveTaskService.getTasksByStatus(Task.TaskStatus.PENDING, 0, 20, 1L))
                .thenReturn(Flux.fromIterable(tasks));

        webTestClient.get()
                .uri("/api/v2/reactive/tasks/status/PENDING?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskPageResponse.class)
                .value(response -> {
                    assert response.getContent().size() == 1;
                    assert response.getContent().get(0).getStatus() == Task.TaskStatus.PENDING;
                });
    }

    @Test
    @WithMockUser
    @DisplayName("Should get task statistics")
    void shouldGetTaskStatistics() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        Map<String, Long> stats = Map.of(
                "pending", 5L,
                "in_progress", 3L,
                "completed", 10L,
                "cancelled", 1L
        );
        when(reactiveTaskService.getTaskStats(1L)).thenReturn(Mono.just(stats));

        webTestClient.get()
                .uri("/api/v2/reactive/tasks/stats/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    assertEquals(5L, ((Number) response.get("pending")).longValue());
                    assertEquals(3L, ((Number) response.get("in_progress")).longValue());
                    assertEquals(10L, ((Number) response.get("completed")).longValue());
                    assertEquals(1L, ((Number) response.get("cancelled")).longValue());
                });
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle empty task list")
    void shouldHandleEmptyTaskList() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(reactiveTaskService.getAllTasks(0, 20, 1L))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v2/reactive/tasks?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskPageResponse.class)
                .value(response -> {
                    assert response.getContent().isEmpty();
                    assert Boolean.TRUE.equals(response.getEmpty());
                });
    }

    @Test
    @WithMockUser
    @DisplayName("Should use default pagination parameters")
    void shouldUseDefaultPaginationParameters() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(reactiveTaskService.getAllTasks(0, 20, 1L))
                .thenReturn(Flux.fromIterable(List.of(taskResponse)));

        webTestClient.get()
                .uri("/api/v2/reactive/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskPageResponse.class)
                .value(response -> {
                    assert response.getNumber() == 0;
                    assert response.getSize() == 20;
                });
    }
}
