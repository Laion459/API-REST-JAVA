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
 * Repository reativo para operações de leitura de alta performance.
 * Usa R2DBC para acesso não-bloqueante ao PostgreSQL.
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = false)
public interface ReactiveTaskRepository extends R2dbcRepository<Task, Long> {
    
    /**
     * Busca task por ID e user (não deletadas).
     * Retorna Mono.empty() se não encontrada.
     */
    @Query("SELECT * FROM tasks WHERE id = :id AND user_id = :userId AND deleted = false")
    Mono<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * Lista todas as tasks de um usuário (não deletadas) com paginação.
     * Ordenado por createdAt DESC.
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND deleted = false " +
           "ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Task> findByUserIdPaginated(@Param("userId") Long userId, 
                                     @Param("limit") int limit, 
                                     @Param("offset") long offset);
    
    /**
     * Lista tasks por status e usuário (não deletadas) com paginação.
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND status = :status AND deleted = false " +
           "ORDER BY priority DESC, created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Task> findByUserIdAndStatusPaginated(@Param("userId") Long userId,
                                               @Param("status") String status,
                                               @Param("limit") int limit,
                                               @Param("offset") long offset);
    
    /**
     * Conta tasks por status e usuário (não deletadas).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND status = :status AND deleted = false")
    Mono<Long> countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Conta total de tasks de um usuário (não deletadas).
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND deleted = false")
    Mono<Long> countByUserId(@Param("userId") Long userId);
}
