package com.leonardoborges.api.dto;

import com.leonardoborges.api.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private Long id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Integer priority;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
