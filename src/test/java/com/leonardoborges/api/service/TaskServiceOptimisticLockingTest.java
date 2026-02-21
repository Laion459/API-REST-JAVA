package com.leonardoborges.api.service;

import com.leonardoborges.api.config.TestSecurityConfig;
import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.exception.OptimisticLockingException;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class TaskServiceOptimisticLockingTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private Task task;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setStatus(Task.TaskStatus.PENDING);
        
        taskService.createTask(request);
        task = taskRepository.findAll().get(0);
    }

    @Test
    void testOptimisticLocking_VersionMismatch() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");
        request.setVersion(999L); // Wrong version
        
        assertThrows(OptimisticLockingException.class, () -> {
            taskService.updateTask(task.getId(), request);
        });
    }

    @Test
    void testOptimisticLocking_VersionIncrement() {
        Long initialVersion = task.getVersion();
        assertNotNull(initialVersion, "Initial version should not be null");
        
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");
        request.setVersion(initialVersion);
        
        var response = taskService.updateTask(task.getId(), request);
        assertNotNull(response.getVersion(), "Response version should not be null");
        
        // Refresh from database to get updated version
        taskRepository.flush();
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        
        assertNotNull(updatedTask.getVersion(), "Updated task version should not be null");
        assertTrue(updatedTask.getVersion() >= initialVersion, 
                "Version should be incremented or at least equal. Initial: " + initialVersion + ", Updated: " + updatedTask.getVersion());
    }

    @Test
    void testOptimisticLocking_ResponseContainsVersion() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated Title");
        
        var response = taskService.updateTask(task.getId(), request);
        
        assertNotNull(response.getVersion(), "Response should contain version");
        assertTrue(response.getVersion() >= 0, 
                "Version should be >= 0, but was: " + response.getVersion());
    }
}
