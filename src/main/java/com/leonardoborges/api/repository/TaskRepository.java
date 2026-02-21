package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.priority DESC, t.createdAt DESC")
    List<Task> findByStatusOrderByPriority(@Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(@Param("status") Task.TaskStatus status);
    
    Optional<Task> findByIdAndStatus(Long id, Task.TaskStatus status);
}
