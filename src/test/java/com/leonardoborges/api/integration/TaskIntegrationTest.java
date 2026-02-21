package com.leonardoborges.api.integration;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.leonardoborges.api.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/tasks";
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() {
        TaskRequest request = TaskRequest.builder()
                .title("Integration Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();

        HttpEntity<TaskRequest> entity = new HttpEntity<>(request);
        ResponseEntity<TaskResponse> response = restTemplate.postForEntity(baseUrl, entity, TaskResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Task", response.getBody().getTitle());
    }

    @Test
    void shouldGetTaskById() {
        Task task = Task.builder()
                .title("Test Task")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task saved = taskRepository.save(task);
        taskRepository.flush();

        ResponseEntity<TaskResponse> response = restTemplate.getForEntity(
                baseUrl + "/" + saved.getId(), TaskResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(saved.getId(), response.getBody().getId());
    }

    @Test
    void shouldGetAllTasks() {
        Task task1 = Task.builder().title("Task 1").status(Task.TaskStatus.PENDING).build();
        Task task2 = Task.builder().title("Task 2").status(Task.TaskStatus.PENDING).build();
        taskRepository.save(task1);
        taskRepository.save(task2);

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldUpdateTask() {
        Task task = Task.builder()
                .title("Original Title")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task saved = taskRepository.save(task);
        taskRepository.flush();

        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();

        HttpEntity<TaskRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                baseUrl + "/" + saved.getId(), HttpMethod.PUT, entity, TaskResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());
    }

    @Test
    void shouldDeleteTask() {
        Task task = Task.builder()
                .title("Task to Delete")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task saved = taskRepository.save(task);
        Long taskId = saved.getId();
        taskRepository.flush();

        // Verifica que a tarefa existe antes de deletar
        assertTrue(taskRepository.existsById(taskId), "Task should exist before deletion");

        // Deleta a tarefa
        restTemplate.delete(baseUrl + "/" + taskId);
        
        // Verifica se a tarefa foi deletada
        boolean exists = taskRepository.existsById(taskId);
        assertFalse(exists, "Task should be deleted");
    }
}
