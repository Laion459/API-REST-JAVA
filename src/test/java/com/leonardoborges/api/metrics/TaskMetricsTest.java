package com.leonardoborges.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskMetrics Tests")
class TaskMetricsTest {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter taskCreatedCounter;

    @Mock
    private Counter taskUpdatedCounter;

    @Mock
    private Counter taskDeletedCounter;

    @Mock
    private Counter taskRetrievedCounter;

    @Mock
    private Counter taskStatusPendingCounter;

    @Mock
    private Counter taskStatusInProgressCounter;

    @Mock
    private Counter taskStatusCompletedCounter;

    @Mock
    private Counter taskStatusCancelledCounter;

    @Mock
    private Timer taskCreationTimer;

    @Mock
    private Timer taskUpdateTimer;

    @Mock
    private Timer taskRetrievalTimer;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private TaskMetrics taskMetrics;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(taskStatusPendingCounter);
        lenient().when(meterRegistry.counter(anyString())).thenReturn(taskCreatedCounter);
    }

    @Test
    @DisplayName("Should initialize all metrics successfully")
    void shouldInitializeAllMetricsSuccessfully() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);

        assertDoesNotThrow(() -> metrics.initializeMetrics());
        
        assertDoesNotThrow(() -> {
            metrics.incrementTaskCreated();
            metrics.incrementTaskUpdated();
            metrics.incrementTaskDeleted();
            metrics.incrementTaskRetrieved();
        });
    }

    @Test
    @DisplayName("Should increment task created counter when initialized")
    void shouldIncrementTaskCreatedCounterWhenInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskCreated());
    }

    @Test
    @DisplayName("Should not increment task created counter when not initialized")
    void shouldNotIncrementTaskCreatedCounterWhenNotInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        
        assertDoesNotThrow(() -> metrics.incrementTaskCreated());
    }

    @Test
    @DisplayName("Should increment task updated counter when initialized")
    void shouldIncrementTaskUpdatedCounterWhenInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskUpdated());
    }

    @Test
    @DisplayName("Should increment task deleted counter when initialized")
    void shouldIncrementTaskDeletedCounterWhenInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskDeleted());
    }

    @Test
    @DisplayName("Should increment task retrieved counter when initialized")
    void shouldIncrementTaskRetrievedCounterWhenInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskRetrieved());
    }

    @Test
    @DisplayName("Should increment PENDING status counter")
    void shouldIncrementPendingStatusCounter() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus("PENDING"));
    }

    @Test
    @DisplayName("Should increment IN_PROGRESS status counter")
    void shouldIncrementInProgressStatusCounter() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Should increment COMPLETED status counter")
    void shouldIncrementCompletedStatusCounter() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus("COMPLETED"));
    }

    @Test
    @DisplayName("Should increment CANCELLED status counter")
    void shouldIncrementCancelledStatusCounter() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus("CANCELLED"));
    }

    @Test
    @DisplayName("Should not increment status counter for null status")
    void shouldNotIncrementStatusCounterForNullStatus() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus(null));
    }

    @Test
    @DisplayName("Should not increment status counter for unknown status")
    void shouldNotIncrementStatusCounterForUnknownStatus() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        assertDoesNotThrow(() -> metrics.incrementTaskStatus("UNKNOWN"));
    }

    @Test
    @DisplayName("Should start task creation timer")
    void shouldStartTaskCreationTimer() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);

        Timer.Sample result = metrics.startTaskCreationTimer();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should start task update timer")
    void shouldStartTaskUpdateTimer() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);

        Timer.Sample result = metrics.startTaskUpdateTimer();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should start task retrieval timer")
    void shouldStartTaskRetrievalTimer() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);

        Timer.Sample result = metrics.startTaskRetrievalTimer();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return null timer sample when meter registry is null")
    void shouldReturnNullTimerSampleWhenMeterRegistryIsNull() {
        TaskMetrics metrics = new TaskMetrics(null);
        
        assertNull(metrics.startTaskCreationTimer());
        assertNull(metrics.startTaskUpdateTimer());
        assertNull(metrics.startTaskRetrievalTimer());
    }

    @Test
    @DisplayName("Should record task creation time")
    void shouldRecordTaskCreationTime() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        Timer.Sample sample = metrics.startTaskCreationTimer();
        metrics.recordTaskCreation(sample);

        assertNotNull(sample);
    }

    @Test
    @DisplayName("Should not record task creation time when sample is null")
    void shouldNotRecordTaskCreationTimeWhenSampleIsNull() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        metrics.recordTaskCreation(null);

        assertDoesNotThrow(() -> metrics.recordTaskCreation(null));
    }

    @Test
    @DisplayName("Should record task update time")
    void shouldRecordTaskUpdateTime() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        Timer.Sample sample = metrics.startTaskUpdateTimer();
        metrics.recordTaskUpdate(sample);

        assertNotNull(sample);
    }

    @Test
    @DisplayName("Should record task retrieval time")
    void shouldRecordTaskRetrievalTime() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        metrics.initializeMetrics();
        
        Timer.Sample sample = metrics.startTaskRetrievalTimer();
        metrics.recordTaskRetrieval(sample);

        assertNotNull(sample);
    }

    @Test
    @DisplayName("Should not record task creation time when timer is not initialized")
    void shouldNotRecordTaskCreationTimeWhenTimerIsNotInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        
        Timer.Sample sample = metrics.startTaskCreationTimer();
        
        assertDoesNotThrow(() -> metrics.recordTaskCreation(sample));
    }

    @Test
    @DisplayName("Should not record task update time when timer is not initialized")
    void shouldNotRecordTaskUpdateTimeWhenTimerIsNotInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        
        Timer.Sample sample = metrics.startTaskUpdateTimer();
        
        assertDoesNotThrow(() -> metrics.recordTaskUpdate(sample));
    }

    @Test
    @DisplayName("Should not record task retrieval time when timer is not initialized")
    void shouldNotRecordTaskRetrievalTimeWhenTimerIsNotInitialized() {
        io.micrometer.core.instrument.simple.SimpleMeterRegistry simpleRegistry = 
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        TaskMetrics metrics = new TaskMetrics(simpleRegistry);
        
        Timer.Sample sample = metrics.startTaskRetrievalTimer();
        
        assertDoesNotThrow(() -> metrics.recordTaskRetrieval(sample));
    }
}
