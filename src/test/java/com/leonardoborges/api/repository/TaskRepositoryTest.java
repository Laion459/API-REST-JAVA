package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveTask() {
        Task task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .build();

        Task saved = taskRepository.save(task);

        assertNotNull(saved.getId());
        assertEquals("Test Task", saved.getTitle());
    }

    @Test
    void shouldFindTaskById() {
        Task task = Task.builder()
                .title("Test Task")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task saved = taskRepository.save(task);

        Optional<Task> found = taskRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Task", found.get().getTitle());
    }

    @Test
    void shouldFindTasksByStatus() {
        Task task1 = Task.builder()
                .title("Task 1")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task task2 = Task.builder()
                .title("Task 2")
                .status(Task.TaskStatus.COMPLETED)
                .build();
        taskRepository.save(task1);
        taskRepository.save(task2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> pendingTasks = taskRepository.findByStatus(Task.TaskStatus.PENDING, pageable);

        assertEquals(1, pendingTasks.getTotalElements());
        assertEquals("Task 1", pendingTasks.getContent().get(0).getTitle());
    }

    @Test
    void shouldCountTasksByStatus() {
        Task task1 = Task.builder()
                .title("Task 1")
                .status(Task.TaskStatus.PENDING)
                .build();
        Task task2 = Task.builder()
                .title("Task 2")
                .status(Task.TaskStatus.PENDING)
                .build();
        taskRepository.save(task1);
        taskRepository.save(task2);

        Long count = taskRepository.countByStatus(Task.TaskStatus.PENDING);

        assertEquals(2, count);
    }
}
