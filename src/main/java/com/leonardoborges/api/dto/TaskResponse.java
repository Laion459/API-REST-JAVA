package com.leonardoborges.api.dto;

import com.leonardoborges.api.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO with task information")
public class TaskResponse {
    
    @Schema(description = "Unique task ID", example = "1")
    private Long id;
    
    @Schema(description = "Task title", example = "Implement feature X")
    private String title;
    
    @Schema(description = "Detailed task description", example = "Implement new functionality with tests")
    private String description;
    
    @Schema(description = "Current task status", example = "PENDING")
    private Task.TaskStatus status;
    
    @Schema(description = "Task priority", example = "1")
    private Integer priority;
    
    @Schema(description = "Task version for optimistic locking", example = "1")
    private Long version;
    
    @Schema(description = "Task creation date", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update date", example = "2025-01-15T14:45:00")
    private LocalDateTime updatedAt;
}
