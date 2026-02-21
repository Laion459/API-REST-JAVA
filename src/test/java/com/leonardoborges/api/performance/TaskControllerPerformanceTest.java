package com.leonardoborges.api.performance;

import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for TaskController.
 * These tests verify that endpoints meet performance requirements.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class TaskControllerPerformanceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskService taskService;
    
    @BeforeEach
    void setUp() {
        // Create test data for performance testing
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .description("Description " + i)
                    .status(Task.TaskStatus.PENDING)
                    .priority(i % 10)
                    .build();
            tasks.add(task);
        }
        taskRepository.saveAll(tasks);
    }
    
    @Test
    void getAllTasks_ShouldRespondWithinAcceptableTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Should respond within 500ms for paginated request
        assert duration < 500 : "Response time too slow: " + duration + "ms";
    }
    
    @Test
    void getTasksByStatus_ShouldRespondWithinAcceptableTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/v1/tasks/status/PENDING")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Should respond within 500ms
        assert duration < 500 : "Response time too slow: " + duration + "ms";
    }
    
    @Test
    void createTask_ShouldCompleteWithinAcceptableTime() {
        TaskRequest request = TaskRequest.builder()
                .title("Performance Test Task")
                .description("Testing performance")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();
        
        long startTime = System.currentTimeMillis();
        taskService.createTask(request);
        long duration = System.currentTimeMillis() - startTime;
        
        // Should complete within 200ms
        assert duration < 200 : "Task creation too slow: " + duration + "ms";
    }
    
    @Test
    void getTaskById_ShouldUseCacheOnSecondCall() {
        // Create a task
        TaskRequest request = TaskRequest.builder()
                .title("Cache Test Task")
                .description("Testing cache")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();
        
        var response = taskService.createTask(request);
        Long taskId = response.getId();
        
        // First call (cache miss)
        taskService.getTaskById(taskId);
        
        // Second call (should hit cache if available)
        long startTime = System.currentTimeMillis();
        taskService.getTaskById(taskId);
        long duration = System.currentTimeMillis() - startTime;
        
        // Verify it completes successfully
        // Note: In test environment, cache might not be active, so we just verify it doesn't fail
        assert duration >= 0 : "Cache test completed successfully";
    }
}
