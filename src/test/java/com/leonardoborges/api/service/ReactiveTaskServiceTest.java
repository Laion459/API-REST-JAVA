package com.leonardoborges.api.service;

import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.reactive.ReactiveTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactiveTaskService Tests")
class ReactiveTaskServiceTest {

    @Mock
    private ReactiveTaskRepository reactiveTaskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> reactiveValueOperations;

    @InjectMocks
    private ReactiveTaskService reactiveTaskService;

    private Task task;
    private TaskResponse taskResponse;
    private Long userId = 1L;
    private Long taskId = 1L;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        lenient().when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    }

    @Test
    @DisplayName("Should get task by ID from cache when cache hit")
    void shouldGetTaskByIdFromCacheWhenCacheHit() {
        String cacheKey = "reactive:task:" + taskId + ":" + userId;
        
        when(reactiveValueOperations.get(cacheKey)).thenReturn(Mono.just(taskResponse));
        lenient().when(reactiveTaskRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Mono.just(task));
        lenient().when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        lenient().when(reactiveValueOperations.set(eq(cacheKey), eq(taskResponse), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(reactiveTaskService.getTaskById(taskId, userId))
                .expectNext(taskResponse)
                .verifyComplete();

        verify(reactiveValueOperations).get(cacheKey);
    }

    @Test
    @DisplayName("Should get task by ID from repository when not in cache")
    void shouldGetTaskByIdFromRepositoryWhenNotInCache() {
        String cacheKey = "reactive:task:" + taskId + ":" + userId;
        when(reactiveValueOperations.get(cacheKey)).thenReturn(Mono.empty());
        when(reactiveTaskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Mono.just(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        when(reactiveValueOperations.set(eq(cacheKey), eq(taskResponse), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(reactiveTaskService.getTaskById(taskId, userId))
                .expectNext(taskResponse)
                .verifyComplete();

        verify(reactiveValueOperations).get(cacheKey);
        verify(reactiveTaskRepository).findByIdAndUserId(taskId, userId);
        verify(reactiveValueOperations).set(eq(cacheKey), eq(taskResponse), any(Duration.class));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task not found")
    void shouldThrowTaskNotFoundExceptionWhenTaskNotFound() {
        String cacheKey = "reactive:task:" + taskId + ":" + userId;
        when(reactiveValueOperations.get(cacheKey)).thenReturn(Mono.empty());
        when(reactiveTaskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Mono.empty());

        StepVerifier.create(reactiveTaskService.getTaskById(taskId, userId))
                .expectError(TaskNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should get all tasks with pagination")
    void shouldGetAllTasksWithPagination() {
        int page = 0;
        int size = 20;
        long offset = 0L;

        when(reactiveTaskRepository.findByUserIdPaginated(userId, size, offset))
                .thenReturn(Flux.just(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        StepVerifier.create(reactiveTaskService.getAllTasks(page, size, userId))
                .expectNext(taskResponse)
                .verifyComplete();

        verify(reactiveTaskRepository).findByUserIdPaginated(userId, size, offset);
    }

    @Test
    @DisplayName("Should get tasks by status with pagination")
    void shouldGetTasksByStatusWithPagination() {
        int page = 0;
        int size = 20;
        long offset = 0L;
        Task.TaskStatus status = Task.TaskStatus.PENDING;

        when(reactiveTaskRepository.findByUserIdAndStatusPaginated(userId, status.name(), size, offset))
                .thenReturn(Flux.just(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        StepVerifier.create(reactiveTaskService.getTasksByStatus(status, page, size, userId))
                .expectNext(taskResponse)
                .verifyComplete();

        verify(reactiveTaskRepository).findByUserIdAndStatusPaginated(userId, status.name(), size, offset);
    }

    @Test
    @DisplayName("Should get task statistics")
    void shouldGetTaskStats() {
        Mono<Long> pending = Mono.just(5L);
        Mono<Long> inProgress = Mono.just(3L);
        Mono<Long> completed = Mono.just(10L);
        Mono<Long> cancelled = Mono.just(1L);

        when(reactiveTaskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.PENDING.name()))
                .thenReturn(pending);
        when(reactiveTaskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.IN_PROGRESS.name()))
                .thenReturn(inProgress);
        when(reactiveTaskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.COMPLETED.name()))
                .thenReturn(completed);
        when(reactiveTaskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.CANCELLED.name()))
                .thenReturn(cancelled);

        StepVerifier.create(reactiveTaskService.getTaskStats(userId))
                .expectNextMatches(stats -> {
                    assertEquals(5L, stats.get("pending"));
                    assertEquals(3L, stats.get("in_progress"));
                    assertEquals(10L, stats.get("completed"));
                    assertEquals(1L, stats.get("cancelled"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should evict task cache")
    void shouldEvictTaskCache() {
        String cacheKey = "reactive:task:" + taskId + ":" + userId;
        when(reactiveRedisTemplate.delete(cacheKey)).thenReturn(Mono.just(1L));

        StepVerifier.create(reactiveTaskService.evictTaskCache(taskId, userId))
                .verifyComplete();

        verify(reactiveRedisTemplate).delete(cacheKey);
    }

    @Test
    @DisplayName("Should handle empty task list")
    void shouldHandleEmptyTaskList() {
        int page = 0;
        int size = 20;
        long offset = 0L;

        when(reactiveTaskRepository.findByUserIdPaginated(userId, size, offset))
                .thenReturn(Flux.empty());

        StepVerifier.create(reactiveTaskService.getAllTasks(page, size, userId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should calculate correct offset for pagination")
    void shouldCalculateCorrectOffsetForPagination() {
        int page = 2;
        int size = 10;
        long expectedOffset = 20L;

        when(reactiveTaskRepository.findByUserIdPaginated(userId, size, expectedOffset))
                .thenReturn(Flux.empty());

        StepVerifier.create(reactiveTaskService.getAllTasks(page, size, userId))
                .verifyComplete();

        verify(reactiveTaskRepository).findByUserIdPaginated(userId, size, expectedOffset);
    }
}
