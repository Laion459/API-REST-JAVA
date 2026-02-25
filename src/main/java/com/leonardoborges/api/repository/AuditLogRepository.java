package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findByActionAndDateRange(@Param("action") String action,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);
    
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.success = false AND al.createdAt >= :since")
    long countFailedActionsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al WHERE al.createdAt >= :since GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> countActionsByType(@Param("since") LocalDateTime since);
}
