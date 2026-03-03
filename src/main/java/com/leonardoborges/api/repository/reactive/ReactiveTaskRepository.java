package com.leonardoborges.api.repository.reactive;

import com.leonardoborges.api.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for non-blocking read operations using R2DBC.
 * Uses R2DBC for non-blocking access to PostgreSQL.
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = false)
public interface ReactiveTaskRepository extends R2dbcRepository<Task, Long> {
    
    /**
     * Finds task by ID and user (not deleted).
     * Returns Mono.empty() if not found.
     */
    @Query("SELECT * FROM tasks WHERE id = :id AND user_id = :userId AND deleted = false")
    Mono<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * Lists all tasks of a user (not deleted) with pagination.
     * Ordered by createdAt DESC.
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND deleted = false " +
           "ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Task> findByUserIdPaginated(@Param("userId") Long userId, 
                                     @Param("limit") int limit, 
                                     @Param("offset") long offset);
    
    /**
     * Lists tasks by status and user (not deleted) with pagination.
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND status = :status AND deleted = false " +
           "ORDER BY priority DESC, created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Task> findByUserIdAndStatusPaginated(@Param("userId") Long userId,
                                               @Param("status") String status,
                                               @Param("limit") int limit,
                                               @Param("offset") long offset);
    
    /**
     * Counts tasks by status and user (not deleted).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND status = :status AND deleted = false")
    Mono<Long> countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Counts total tasks of a user (not deleted).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND deleted = false")
    Mono<Long> countByUserId(@Param("userId") Long userId);
}
