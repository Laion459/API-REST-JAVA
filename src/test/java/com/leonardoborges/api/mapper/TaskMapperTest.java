package com.leonardoborges.api.mapper;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskMapper Tests")
class TaskMapperTest {

    private TaskMapper taskMapper;
    private Task task;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskMapper = new TaskMapper();
        
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(Task.TaskStatus.PENDING)
                .priority(1)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskRequest = TaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(2)
                .version(1L)
                .build();
    }

    @Test
    @DisplayName("Should convert Task to TaskResponse")
    void shouldConvertTaskToTaskResponse() {
        TaskResponse response = taskMapper.toResponse(task);

        assertNotNull(response);
        assertEquals(task.getId(), response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getDescription(), response.getDescription());
        assertEquals(task.getStatus(), response.getStatus());
        assertEquals(task.getPriority(), response.getPriority());
        assertEquals(task.getVersion(), response.getVersion());
        assertEquals(task.getCreatedAt(), response.getCreatedAt());
        assertEquals(task.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    @DisplayName("Should return null when Task is null")
    void shouldReturnNullWhenTaskIsNull() {
        TaskResponse response = taskMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    @DisplayName("Should convert Page of Task to Page of TaskResponse")
    void shouldConvertPageOfTaskToPageOfTaskResponse() {
        List<Task> tasks = Arrays.asList(task, task);
        Page<Task> taskPage = new PageImpl<>(tasks, PageRequest.of(0, 20), 2);

        Page<TaskResponse> responsePage = taskMapper.toResponsePage(taskPage);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(task.getId(), responsePage.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should convert TaskRequest to Task")
    void shouldConvertTaskRequestToTask() {
        Task entity = taskMapper.toEntity(taskRequest);

        assertNotNull(entity);
        assertEquals(taskRequest.getTitle(), entity.getTitle());
        assertEquals(taskRequest.getDescription(), entity.getDescription());
        assertEquals(taskRequest.getStatus(), entity.getStatus());
        assertEquals(taskRequest.getPriority(), entity.getPriority());
    }

    @Test
    @DisplayName("Should use PENDING status when TaskRequest has no status")
    void shouldUsePendingStatusWhenTaskRequestHasNoStatus() {
        taskRequest.setStatus(null);
        
        Task entity = taskMapper.toEntity(taskRequest);

        assertNotNull(entity);
        assertEquals(Task.TaskStatus.PENDING, entity.getStatus());
    }

    @Test
    @DisplayName("Should return null when TaskRequest is null")
    void shouldReturnNullWhenTaskRequestIsNull() {
        Task entity = taskMapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    @DisplayName("Should update Task from TaskRequest")
    void shouldUpdateTaskFromTaskRequest() {
        taskMapper.updateEntityFromRequest(task, taskRequest);

        assertEquals(taskRequest.getTitle(), task.getTitle());
        assertEquals(taskRequest.getDescription(), task.getDescription());
        assertEquals(taskRequest.getStatus(), task.getStatus());
        assertEquals(taskRequest.getPriority(), task.getPriority());
    }

    @Test
    @DisplayName("Should not update status when TaskRequest status is null")
    void shouldNotUpdateStatusWhenTaskRequestStatusIsNull() {
        Task.TaskStatus originalStatus = task.getStatus();
        taskRequest.setStatus(null);
        
        taskMapper.updateEntityFromRequest(task, taskRequest);

        assertEquals(originalStatus, task.getStatus());
    }

    @Test
    @DisplayName("Should not update priority when TaskRequest priority is null")
    void shouldNotUpdatePriorityWhenTaskRequestPriorityIsNull() {
        Integer originalPriority = task.getPriority();
        taskRequest.setPriority(null);
        
        taskMapper.updateEntityFromRequest(task, taskRequest);

        assertEquals(originalPriority, task.getPriority());
    }

    @Test
    @DisplayName("Should do nothing when Task is null")
    void shouldDoNothingWhenTaskIsNull() {
        assertDoesNotThrow(() -> {
            taskMapper.updateEntityFromRequest(null, taskRequest);
        });
    }

    @Test
    @DisplayName("Should do nothing when TaskRequest is null")
    void shouldDoNothingWhenTaskRequestIsNull() {
        assertDoesNotThrow(() -> {
            taskMapper.updateEntityFromRequest(task, null);
        });
    }
}
