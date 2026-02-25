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
 * Service reativo para operações de leitura de alta performance.
 * Usa programação reativa (Mono/Flux) para melhor escalabilidade.
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
     * Busca task por ID de forma reativa (não-bloqueante).
     * Usa cache Redis reativo para melhor performance.
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
     * Lista todas as tasks de forma reativa com paginação.
     * Otimizado para alta concorrência.
     */
    public Flux<TaskResponse> getAllTasks(int page, int size, Long userId) {
        long offset = (long) page * size;
        
        return reactiveTaskRepository.findByUserIdPaginated(userId, size, offset)
                .map(taskMapper::toResponse)
                .doOnComplete(() -> log.debug("Tasks retrieved reactively: page={}, size={}", page, size));
    }
    
    /**
     * Lista tasks por status de forma reativa com paginação.
     */
    public Flux<TaskResponse> getTasksByStatus(Task.TaskStatus status, int page, int size, Long userId) {
        long offset = (long) page * size;
        
        return reactiveTaskRepository.findByUserIdAndStatusPaginated(
                        userId, status.name(), size, offset)
                .map(taskMapper::toResponse)
                .doOnComplete(() -> log.debug("Tasks by status retrieved reactively: status={}", status));
    }
    
    /**
     * Retorna estatísticas de tasks de forma reativa.
     * Otimizado para leitura rápida.
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
     * Invalida cache de uma task específica.
     */
    public Mono<Void> evictTaskCache(Long taskId, Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + taskId + ":" + userId;
        return reactiveRedisTemplate.delete(cacheKey).then();
    }
    
}
