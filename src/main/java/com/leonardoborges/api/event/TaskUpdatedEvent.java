package com.leonardoborges.api.event;

import com.leonardoborges.api.model.Task;
import lombok.Getter;

@Getter
public class TaskUpdatedEvent {
    
    private final Long taskId;
    private final Task oldTask;
    private final Task.TaskStatus oldStatus;
    private final Task updatedTask;
    
    public TaskUpdatedEvent(Long taskId, Task oldTask, Task.TaskStatus oldStatus, Task updatedTask) {
        this.taskId = taskId;
        this.oldTask = oldTask;
        this.oldStatus = oldStatus;
        this.updatedTask = updatedTask;
    }
}
