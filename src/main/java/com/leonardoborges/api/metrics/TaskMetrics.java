package com.leonardoborges.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for task operations.
 * Tracks business metrics for monitoring and observability.
 */
@Component
@RequiredArgsConstructor
public class TaskMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // Counters for task operations
    private Counter taskCreatedCounter;
    private Counter taskUpdatedCounter;
    private Counter taskDeletedCounter;
    private Counter taskRetrievedCounter;
    
    // Counters by status
    private Counter taskStatusPendingCounter;
    private Counter taskStatusInProgressCounter;
    private Counter taskStatusCompletedCounter;
    private Counter taskStatusCancelledCounter;
    
    // Timers for operation duration
    private Timer taskCreationTimer;
    private Timer taskUpdateTimer;
    private Timer taskRetrievalTimer;
    
    public void initializeMetrics() {
        // Initialize counters
        taskCreatedCounter = Counter.builder("tasks.created")
                .description("Total number of tasks created")
                .register(meterRegistry);
        
        taskUpdatedCounter = Counter.builder("tasks.updated")
                .description("Total number of tasks updated")
                .register(meterRegistry);
        
        taskDeletedCounter = Counter.builder("tasks.deleted")
                .description("Total number of tasks deleted")
                .register(meterRegistry);
        
        taskRetrievedCounter = Counter.builder("tasks.retrieved")
                .description("Total number of tasks retrieved")
                .register(meterRegistry);
        
        // Status counters
        taskStatusPendingCounter = Counter.builder("tasks.status")
                .description("Tasks by status")
                .tag("status", "PENDING")
                .register(meterRegistry);
        
        taskStatusInProgressCounter = Counter.builder("tasks.status")
                .description("Tasks by status")
                .tag("status", "IN_PROGRESS")
                .register(meterRegistry);
        
        taskStatusCompletedCounter = Counter.builder("tasks.status")
                .description("Tasks by status")
                .tag("status", "COMPLETED")
                .register(meterRegistry);
        
        taskStatusCancelledCounter = Counter.builder("tasks.status")
                .description("Tasks by status")
                .tag("status", "CANCELLED")
                .register(meterRegistry);
        
        // Timers
        taskCreationTimer = Timer.builder("tasks.creation.duration")
                .description("Time taken to create a task")
                .register(meterRegistry);
        
        taskUpdateTimer = Timer.builder("tasks.update.duration")
                .description("Time taken to update a task")
                .register(meterRegistry);
        
        taskRetrievalTimer = Timer.builder("tasks.retrieval.duration")
                .description("Time taken to retrieve tasks")
                .register(meterRegistry);
    }
    
    public void incrementTaskCreated() {
        if (taskCreatedCounter != null) {
            taskCreatedCounter.increment();
        }
    }
    
    public void incrementTaskUpdated() {
        if (taskUpdatedCounter != null) {
            taskUpdatedCounter.increment();
        }
    }
    
    public void incrementTaskDeleted() {
        if (taskDeletedCounter != null) {
            taskDeletedCounter.increment();
        }
    }
    
    public void incrementTaskRetrieved() {
        if (taskRetrievedCounter != null) {
            taskRetrievedCounter.increment();
        }
    }
    
    public void incrementTaskStatus(String status) {
        if (status == null) return;
        switch (status) {
            case "PENDING" -> {
                if (taskStatusPendingCounter != null) taskStatusPendingCounter.increment();
            }
            case "IN_PROGRESS" -> {
                if (taskStatusInProgressCounter != null) taskStatusInProgressCounter.increment();
            }
            case "COMPLETED" -> {
                if (taskStatusCompletedCounter != null) taskStatusCompletedCounter.increment();
            }
            case "CANCELLED" -> {
                if (taskStatusCancelledCounter != null) taskStatusCancelledCounter.increment();
            }
        }
    }
    
    public Timer.Sample startTaskCreationTimer() {
        return meterRegistry != null ? Timer.start(meterRegistry) : null;
    }
    
    public Timer.Sample startTaskUpdateTimer() {
        return meterRegistry != null ? Timer.start(meterRegistry) : null;
    }
    
    public Timer.Sample startTaskRetrievalTimer() {
        return meterRegistry != null ? Timer.start(meterRegistry) : null;
    }
    
    public void recordTaskCreation(Timer.Sample sample) {
        if (taskCreationTimer != null && sample != null) {
            sample.stop(taskCreationTimer);
        }
    }
    
    public void recordTaskUpdate(Timer.Sample sample) {
        if (taskUpdateTimer != null && sample != null) {
            sample.stop(taskUpdateTimer);
        }
    }
    
    public void recordTaskRetrieval(Timer.Sample sample) {
        if (taskRetrievalTimer != null && sample != null) {
            sample.stop(taskRetrievalTimer);
        }
    }
}
