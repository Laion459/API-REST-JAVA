package com.leonardoborges.api.repository;

import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import com.leonardoborges.api.util.TestBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para TaskRepository usando @DataJpaTest.
 * 
 * Boas práticas aplicadas:
 * - @DataJpaTest carrega apenas camada JPA (rápido e focado)
 * - Usa repositórios diretamente para evitar problemas com detached entities
 * - Testa queries customizadas do repositório
 * - Isolamento completo entre testes
 * - Nomes descritivos com @DisplayName
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = TestBuilders.defaultUser().build();
        testUser = userRepository.saveAndFlush(testUser);

        testTask = TestBuilders.defaultTask()
                .user(testUser)
                .build();
        testTask = taskRepository.saveAndFlush(testTask);
    }

    @Test
    @DisplayName("Deve encontrar tarefa por ID e usuário")
    void shouldFindTaskByIdAndUser() {
        // When
        Optional<Task> found = taskRepository.findByIdAndUser(testTask.getId(), testUser);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testTask.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Não deve encontrar tarefa deletada")
    void shouldNotFindDeletedTask() {
        // Given
        Task deletedTask = TestBuilders.defaultTask()
                .user(testUser)
                .deleted(true)
                .build();
        deletedTask = taskRepository.saveAndFlush(deletedTask);

        // When
        Optional<Task> found = taskRepository.findByIdAndUser(deletedTask.getId(), testUser);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar tarefas por usuário paginadas")
    void shouldFindTasksByUserPaginated() {
        // Given
        Task task2 = TestBuilders.defaultTask()
                .user(testUser)
                .title("Task 2")
                .build();
        taskRepository.saveAndFlush(task2);

        // When
        Page<Task> page = taskRepository.findByUser(testUser, PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve contar tarefas por usuário")
    void shouldCountTasksByUser() {
        // Given
        Task task2 = TestBuilders.defaultTask()
                .user(testUser)
                .title("Task 2")
                .build();
        taskRepository.saveAndFlush(task2);

        // When
        Long count = taskRepository.countByUser(testUser);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve encontrar tarefas por status")
    void shouldFindTasksByStatus() {
        // Given
        Task completedTask = TestBuilders.defaultTask()
                .user(testUser)
                .status(Task.TaskStatus.COMPLETED)
                .title("Completed Task")
                .build();
        taskRepository.saveAndFlush(completedTask);

        // When
        Page<Task> page = taskRepository.findByUserAndStatus(
                testUser,
                Task.TaskStatus.COMPLETED,
                PageRequest.of(0, 10)
        );

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
    }
}
