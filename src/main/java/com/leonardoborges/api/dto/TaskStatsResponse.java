package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Task statistics by status")
public class TaskStatsResponse {
    
    @Schema(description = "Number of pending tasks", example = "5")
    private Long pending;
    
    @Schema(description = "Number of in-progress tasks", example = "3")
    private Long inProgress;
    
    @Schema(description = "Number of completed tasks", example = "10")
    private Long completed;
    
    @Schema(description = "Number of cancelled tasks", example = "1")
    private Long cancelled;
}
