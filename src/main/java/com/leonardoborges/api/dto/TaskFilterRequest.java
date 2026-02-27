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
@Schema(description = "Advanced filter DTO for task queries")
public class TaskFilterRequest {
    
    @Schema(description = "Filter by task status", example = "PENDING")
    private Task.TaskStatus status;
    
    @Schema(description = "Filter by minimum priority", example = "5")
    private Integer minPriority;
    
    @Schema(description = "Filter by maximum priority", example = "10")
    private Integer maxPriority;
    
    @Schema(description = "Filter by title containing text (case-insensitive)", example = "implement")
    private String titleContains;
    
    @Schema(description = "Filter by description containing text (case-insensitive)", example = "feature")
    private String descriptionContains;
    
    @Schema(description = "Filter tasks created after this date", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAfter;
    
    @Schema(description = "Filter tasks created before this date", example = "2024-12-31T23:59:59")
    private LocalDateTime createdBefore;
    
    @Schema(description = "Filter tasks updated after this date", example = "2024-01-01T00:00:00")
    private LocalDateTime updatedAfter;
    
    @Schema(description = "Filter tasks updated before this date", example = "2024-12-31T23:59:59")
    private LocalDateTime updatedBefore;
    
    @Schema(description = "Filter by user ID (admin only)", example = "1")
    private Long userId;
    
    @Schema(description = "Include deleted tasks (admin only)", example = "false")
    private Boolean includeDeleted;
}
