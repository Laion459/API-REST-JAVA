package com.leonardoborges.api.repository;

import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepositoryCustom {
    
    Page<Task> findTasksWithFilters(User user, TaskFilterRequest filters, Pageable pageable);
}
