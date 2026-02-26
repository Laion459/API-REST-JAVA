package com.leonardoborges.api.service;

import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.mapper.TaskMapper;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.reactive.ReactiveTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Reactive service for high-performance read operations.
 * Uses reactive programming (Mono/Flux) for better scalability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = false)
public class ReactiveTaskService {
    
    private final ReactiveTaskRepository reactiveTaskRepository;
    private final TaskMapper taskMapper;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "reactive:task:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    
    /**
     * Retrieves task by ID reactively (non-blocking).
     * Uses reactive Redis cache for better performance.
     */
    public Mono<TaskResponse> getTaskById(Long id, Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + id + ":" + userId;
        
        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .cast(TaskResponse.class)
                .switchIfEmpty(
                        reactiveTaskRepository.findByIdAndUserId(id, userId)
                                .map(taskMapper::toResponse)
                                .flatMap(response -> 
                                        reactiveRedisTemplate.opsForValue()
                                                .set(cacheKey, response, CACHE_TTL)
                                                .thenReturn(response)
                                )
                )
                .doOnNext(response -> log.debug("Task retrieved reactively: {}", id))
                .switchIfEmpty(Mono.error(new com.leonardoborges.api.exception.TaskNotFoundException(id)));
    }
    
    /**
     * Lists all tasks reactively with pagination.
     * Optimized for high concurrency.
     */
    public Flux<TaskResponse> getAllTasks(int page, int size, Long userId) {
        long offset = (long) page * size;
        
        return reactiveTaskRepository.findByUserIdPaginated(userId, size, offset)
                .map(taskMapper::toResponse)
                .doOnComplete(() -> log.debug("Tasks retrieved reactively: page={}, size={}", page, size));
    }
    
    /**
     * Lists tasks by status reactively with pagination.
     */
    public Flux<TaskResponse> getTasksByStatus(Task.TaskStatus status, int page, int size, Long userId) {
        long offset = (long) page * size;
        
        return reactiveTaskRepository.findByUserIdAndStatusPaginated(
                        userId, status.name(), size, offset)
                .map(taskMapper::toResponse)
                .doOnComplete(() -> log.debug("Tasks by status retrieved reactively: status={}", status));
    }
    
    /**
     * Returns task statistics reactively.
     * Optimized for fast reads.
     */
    public Mono<Map<String, Long>> getTaskStats(Long userId) {
        Mono<Long> pending = reactiveTaskRepository.countByUserIdAndStatus(
                userId, Task.TaskStatus.PENDING.name());
        Mono<Long> inProgress = reactiveTaskRepository.countByUserIdAndStatus(
                userId, Task.TaskStatus.IN_PROGRESS.name());
        Mono<Long> completed = reactiveTaskRepository.countByUserIdAndStatus(
                userId, Task.TaskStatus.COMPLETED.name());
        Mono<Long> cancelled = reactiveTaskRepository.countByUserIdAndStatus(
                userId, Task.TaskStatus.CANCELLED.name());
        
        return Mono.zip(pending, inProgress, completed, cancelled)
                .map(tuple -> Map.of(
                        "pending", tuple.getT1(),
                        "in_progress", tuple.getT2(),
                        "completed", tuple.getT3(),
                        "cancelled", tuple.getT4()
                ))
                .doOnNext(stats -> log.debug("Task stats retrieved reactively"));
    }
    
    /**
     * Invalidates cache for a specific task.
     */
    public Mono<Void> evictTaskCache(Long taskId, Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + taskId + ":" + userId;
        return reactiveRedisTemplate.delete(cacheKey).then();
    }
    
}
