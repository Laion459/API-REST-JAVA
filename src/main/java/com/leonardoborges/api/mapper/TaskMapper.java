package com.leonardoborges.api.mapper;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .version(task.getVersion())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    
    public Page<TaskResponse> toResponsePage(Page<Task> taskPage) {
        return taskPage.map(this::toResponse);
    }
    
    public Task toEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.PENDING)
                .priority(request.getPriority())
                .build();
    }
    
    public void updateEntityFromRequest(Task task, TaskRequest request) {
        if (task == null || request == null) {
            return;
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
    }
}
