package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/reactive/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reactive Tasks", description = "API reativa para tarefas usando WebFlux (programação concorrente)")
public class ReactiveTaskController {
    
    private final TaskService taskService;
    
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream de tarefas (SSE)", description = "Retorna stream de tarefas usando Server-Sent Events para programação reativa e concorrente")
    public Flux<TaskResponse> streamTasks() {
        log.info("Streaming tasks using reactive programming");
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(sequence -> {
                    Pageable pageable = PageRequest.of(0, 10);
                    List<TaskResponse> tasks = taskService.getAllTasks(pageable).getContent();
                    return Flux.fromIterable(tasks);
                })
                .take(100) // Limitar a 100 itens
                .doOnNext(task -> log.debug("Streaming task: {}", task.getId()));
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar tarefas por status (reativo)", description = "Retorna tarefas filtradas por status usando programação reativa")
    public Mono<List<TaskResponse>> getTasksByStatusReactive(@PathVariable Task.TaskStatus status) {
        log.debug("Fetching tasks by status reactively: {}", status);
        Pageable pageable = PageRequest.of(0, 100);
        return Mono.fromSupplier(() -> 
                taskService.getTasksByStatus(status, pageable).getContent()
        ).subscribeOn(Schedulers.boundedElastic());
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Estatísticas em tempo real", description = "Retorna estatísticas de tarefas usando programação reativa")
    public Mono<java.util.Map<String, Long>> getStatsReactive() {
        log.debug("Fetching stats reactively");
        return Mono.fromSupplier(() -> 
                java.util.Map.of(
                        "pending", taskService.getTaskCountByStatus(Task.TaskStatus.PENDING),
                        "in_progress", taskService.getTaskCountByStatus(Task.TaskStatus.IN_PROGRESS),
                        "completed", taskService.getTaskCountByStatus(Task.TaskStatus.COMPLETED),
                        "cancelled", taskService.getTaskCountByStatus(Task.TaskStatus.CANCELLED)
                )
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
