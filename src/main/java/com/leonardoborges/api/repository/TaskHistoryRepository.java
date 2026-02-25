package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.TaskHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    
    Page<TaskHistory> findByTaskId(Long taskId, Pageable pageable);
    
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(Long taskId);
    
    @Query("SELECT th FROM TaskHistory th WHERE th.taskId = :taskId AND th.fieldName = :fieldName ORDER BY th.changedAt DESC")
    List<TaskHistory> findByTaskIdAndFieldName(@Param("taskId") Long taskId, @Param("fieldName") String fieldName);
    
    @Query("SELECT th FROM TaskHistory th WHERE th.taskId = :taskId AND th.changedAt BETWEEN :startDate AND :endDate ORDER BY th.changedAt DESC")
    Page<TaskHistory> findByTaskIdAndDateRange(@Param("taskId") Long taskId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
}
