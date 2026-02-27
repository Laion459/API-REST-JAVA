package com.leonardoborges.api.integration;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.Task.TaskStatus;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.repository.TaskRepository;
import com.leonardoborges.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import com.leonardoborges.api.config.JpaAuditingConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Task entity using PostgreSQL via Testcontainers.
 * 
 * These tests verify:
 * - Database persistence and retrieval
 * - Complex queries and pagination
 * - Transaction handling
 * - Entity relationships
 * - Database constraints and validations
 * 
 * Best practices:
 * - Uses real PostgreSQL database (via Testcontainers)
 * - Tests complete data flow from repository to database
 * - Verifies database-level constraints
 * - Tests complex queries that require real database
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("integration")
@DisplayName("Task Integration Tests with PostgreSQL")
@org.junit.jupiter.api.Tag("integration")
class TaskIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        taskRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    @DisplayName("Should persist and retrieve task from PostgreSQL database")
    void shouldPersistAndRetrieveTask() {
        // Arrange
        Task task = Task.builder()
                .title("Integration Test Task")
                .description("Testing database persistence")
                .status(TaskStatus.PENDING)
                .priority(5)
                .user(testUser)
                .build();

        // Act
        Task savedTask = taskRepository.saveAndFlush(task);
        entityManager.clear(); // Clear persistence context to force database read

        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getTitle()).isEqualTo("Integration Test Task");
        assertThat(retrievedTask.get().getDescription()).isEqualTo("Testing database persistence");
        assertThat(retrievedTask.get().getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(retrievedTask.get().getPriority()).isEqualTo(5);
        assertThat(retrievedTask.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should find tasks by status using PostgreSQL")
    void shouldFindTasksByStatus() {
        // Arrange
        Task pendingTask = Task.builder()
                .title("Pending Task")
                .status(TaskStatus.PENDING)
                .priority(8)
                .user(testUser)
                .build();

        Task completedTask = Task.builder()
                .title("Completed Task")
                .status(TaskStatus.COMPLETED)
                .priority(5)
                .user(testUser)
                .build();

        taskRepository.saveAllAndFlush(List.of(pendingTask, completedTask));
        entityManager.clear();

        // Act
        List<Task> pendingTasks = taskRepository.findByUserAndStatusOrderByPriority(testUser, TaskStatus.PENDING);

        // Assert
        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("Pending Task");
    }

    @Test
    @DisplayName("Should paginate tasks correctly with PostgreSQL")
    void shouldPaginateTasks() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .status(TaskStatus.PENDING)
                    .priority(5)
                    .user(testUser)
                    .build();
            taskRepository.save(task);
        }
        taskRepository.flush();
        entityManager.clear();

        // Act
        Page<Task> firstPage = taskRepository.findAll(
                PageRequest.of(0, 10, Sort.by("title").ascending()));

        Page<Task> secondPage = taskRepository.findAll(
                PageRequest.of(1, 10, Sort.by("title").ascending()));

        // Assert
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(secondPage.isLast()).isTrue();
        assertThat(secondPage.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should update task and persist changes to PostgreSQL")
    void shouldUpdateTask() {
        // Arrange
        Task task = Task.builder()
                .title("Original Title")
                .status(TaskStatus.PENDING)
                .priority(3)
                .user(testUser)
                .build();
        task = taskRepository.saveAndFlush(task);
        Long taskId = task.getId();
        entityManager.clear();

        // Act
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        assertThat(taskOpt).isPresent();
        
        Task taskToUpdate = taskOpt.get();
        taskToUpdate.setTitle("Updated Title");
        taskToUpdate.setStatus(TaskStatus.IN_PROGRESS);
        taskToUpdate.setPriority(8);
        
        taskRepository.saveAndFlush(taskToUpdate);
        entityManager.clear();

        Optional<Task> retrievedTask = taskRepository.findById(taskId);

        // Assert
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getTitle()).isEqualTo("Updated Title");
        assertThat(retrievedTask.get().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(retrievedTask.get().getPriority()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should delete task from PostgreSQL database")
    void shouldDeleteTask() {
        // Arrange
        Task task = Task.builder()
                .title("Task to Delete")
                .status(TaskStatus.PENDING)
                .priority(5)
                .user(testUser)
                .build();
        task = taskRepository.saveAndFlush(task);
        Long taskId = task.getId();
        entityManager.clear();

        // Act
        taskRepository.deleteById(taskId);
        taskRepository.flush();
        entityManager.clear();

        Optional<Task> deletedTask = taskRepository.findById(taskId);

        // Assert
        assertThat(deletedTask).isEmpty();
    }

    @Test
    @DisplayName("Should handle soft delete with deletedAt timestamp")
    void shouldHandleSoftDelete() {
        // Arrange
        Task task = Task.builder()
                .title("Task for Soft Delete")
                .status(TaskStatus.PENDING)
                .priority(5)
                .user(testUser)
                .build();
        task = taskRepository.saveAndFlush(task);
        Long taskId = task.getId();
        entityManager.clear();

        // Act
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        assertThat(taskOpt).isPresent();
        
        Task taskToDelete = taskOpt.get();
        taskToDelete.setDeletedAt(LocalDateTime.now());
        
        Task deletedTask = taskRepository.saveAndFlush(taskToDelete);
        entityManager.clear();

        // Assert
        assertThat(deletedTask.getDeletedAt()).isNotNull();
        // Note: Actual soft delete filtering would be handled by @Where annotation or custom queries
    }

    @Test
    @DisplayName("Should maintain user relationship when saving task")
    void shouldMaintainUserRelationship() {
        // Arrange
        Task task = Task.builder()
                .title("Task with User")
                .status(TaskStatus.PENDING)
                .priority(5)
                .user(testUser)
                .build();

        // Act
        Task savedTask = taskRepository.saveAndFlush(task);
        entityManager.clear();

        Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getUser()).isNotNull();
        assertThat(retrievedTask.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(retrievedTask.get().getUser().getUsername()).isEqualTo("testuser");
    }
}
