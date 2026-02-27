package com.leonardoborges.api.event;

import com.leonardoborges.api.model.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskCreatedEvent extends ApplicationEvent {
    
    private final Task task;
    
    public TaskCreatedEvent(Object source, Task task) {
        super(source);
        this.task = task;
    }
}
