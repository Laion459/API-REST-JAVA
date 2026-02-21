package com.leonardoborges.api.model;

import com.leonardoborges.api.constants.TaskConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = TaskConstants.TITLE_MIN_LENGTH, max = TaskConstants.TITLE_MAX_LENGTH, 
          message = "Title must be between " + TaskConstants.TITLE_MIN_LENGTH + " and " + TaskConstants.TITLE_MAX_LENGTH + " characters")
    @Column(nullable = false)
    private String title;
    
    @Size(max = TaskConstants.DESCRIPTION_MAX_LENGTH, 
          message = "Description must not exceed " + TaskConstants.DESCRIPTION_MAX_LENGTH + " characters")
    @Column(length = TaskConstants.DESCRIPTION_MAX_LENGTH)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = TaskConstants.DEFAULT_PRIORITY;
    
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
