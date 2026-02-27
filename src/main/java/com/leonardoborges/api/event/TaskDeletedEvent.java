package com.leonardoborges.api.event;

import com.leonardoborges.api.model.Task;
import lombok.Getter;

@Getter
public class TaskDeletedEvent {
    
    private final Long taskId;
    private final String taskTitle;
    private final Task.TaskStatus status;
    
    public TaskDeletedEvent(Long taskId, String taskTitle, Task.TaskStatus status) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.status = status;
    }
}
