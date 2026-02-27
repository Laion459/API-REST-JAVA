package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, TaskRepositoryCustom {
    
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status = :status AND t.deleted = false")
    Page<Task> findByUserAndStatus(@Param("user") User user, @Param("status") Task.TaskStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.deleted = false")
    Page<Task> findByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user = :user AND t.deleted = false")
    Optional<Task> findByIdAndUser(@Param("id") Long id, @Param("user") User user);
    
    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status = :status AND t.deleted = false ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findByUserAndStatusOrderByPriority(@Param("user") User user, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.status = :status AND t.deleted = false")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.deleted = false")
    Long countByUser(@Param("user") User user);
    
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user = :user AND t.status = :status AND t.deleted = false")
    Optional<Task> findByIdAndUserAndStatus(@Param("id") Long id, @Param("user") User user, @Param("status") Task.TaskStatus status);
    
    // Admin methods (include deleted)
    @Query("SELECT t FROM Task t WHERE t.user = :user")
    Page<Task> findByUserIncludingDeleted(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user = :user")
    Optional<Task> findByIdAndUserIncludingDeleted(@Param("id") Long id, @Param("user") User user);
}
