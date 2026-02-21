package com.leonardoborges.api.service;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.TaskNotFoundException;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task: {}", request.getTitle());
        
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();
        
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        
        return mapToResponse(savedTask);
    }
    
    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return mapToResponse(task);
    }
    
    @Cacheable(value = "tasks", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);
        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }
    
    @Cacheable(value = "tasks", key = "'status-' + #status + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(Task.TaskStatus status, Pageable pageable) {
        log.debug("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }
    
    @Cacheable(value = "taskStats", key = "#status")
    @Transactional(readOnly = true)
    public Long getTaskCountByStatus(Task.TaskStatus status) {
        log.debug("Counting tasks with status: {}", status);
        return taskRepository.countByStatus(status);
    }
    
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats"}, allEntries = true)
    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task with ID: {}", id);
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        
        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());
        
        return mapToResponse(updatedTask);
    }
    
    @Transactional
    @CacheEvict(value = {"tasks", "taskStats"}, allEntries = true)
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
        log.info("Task deleted successfully with ID: {}", id);
    }
    
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
