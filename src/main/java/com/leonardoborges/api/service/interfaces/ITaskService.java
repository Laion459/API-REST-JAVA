package com.leonardoborges.api.service.interfaces;

import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {
    
    TaskResponse createTask(TaskRequest request);
    
    TaskResponse getTaskById(Long id);
    
    Page<TaskResponse> getAllTasks(Pageable pageable);
    
    Page<TaskResponse> getTasksByStatus(Task.TaskStatus status, Pageable pageable);
    
    Page<TaskResponse> getTasksWithFilters(TaskFilterRequest filters, Pageable pageable);
    
    Long getTaskCountByStatus(Task.TaskStatus status);
    
    TaskResponse updateTask(Long id, TaskRequest request);
    
    TaskResponse patchTask(Long id, TaskRequest request);
    
    void deleteTask(Long id);
    
    void restoreTask(Long id);
}
