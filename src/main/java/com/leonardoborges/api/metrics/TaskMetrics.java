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
        taskCreatedCounter.increment();
    }
    
    public void incrementTaskUpdated() {
        taskUpdatedCounter.increment();
    }
    
    public void incrementTaskDeleted() {
        taskDeletedCounter.increment();
    }
    
    public void incrementTaskRetrieved() {
        taskRetrievedCounter.increment();
    }
    
    public void incrementTaskStatus(String status) {
        switch (status) {
            case "PENDING" -> taskStatusPendingCounter.increment();
            case "IN_PROGRESS" -> taskStatusInProgressCounter.increment();
            case "COMPLETED" -> taskStatusCompletedCounter.increment();
            case "CANCELLED" -> taskStatusCancelledCounter.increment();
        }
    }
    
    public Timer.Sample startTaskCreationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public Timer.Sample startTaskUpdateTimer() {
        return Timer.start(meterRegistry);
    }
    
    public Timer.Sample startTaskRetrievalTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordTaskCreation(Timer.Sample sample) {
        sample.stop(taskCreationTimer);
    }
    
    public void recordTaskUpdate(Timer.Sample sample) {
        sample.stop(taskUpdateTimer);
    }
    
    public void recordTaskRetrieval(Timer.Sample sample) {
        sample.stop(taskRetrievalTimer);
    }
}
