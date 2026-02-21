package com.leonardoborges.api.controller;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(
            summary = "Stream de tarefas (SSE)",
            description = "Retorna stream de tarefas usando Server-Sent Events para programação reativa e concorrente. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stream iniciado com sucesso (Server-Sent Events)",
                    content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - Token JWT inválido ou ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas requisições - Rate limit excedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Flux<TaskResponse> streamTasks() {
        log.info("Streaming tasks using reactive programming");
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(sequence -> {
                    Pageable pageable = PageRequest.of(0, TaskConstants.REACTIVE_STREAM_PAGE_SIZE);
                    List<TaskResponse> tasks = taskService.getAllTasks(pageable).getContent();
                    return Flux.fromIterable(tasks);
                })
                .take(TaskConstants.REACTIVE_MAX_ITEMS)
                .doOnNext(task -> log.debug("Streaming task: {}", task.getId()));
    }
    
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Buscar tarefas por status (reativo)",
            description = "Retorna tarefas filtradas por status usando programação reativa. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarefas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - Token JWT inválido ou ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas requisições - Rate limit excedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<List<TaskResponse>> getTasksByStatusReactive(@PathVariable Task.TaskStatus status) {
        log.debug("Fetching tasks by status reactively: {}", status);
        Pageable pageable = PageRequest.of(0, TaskConstants.REACTIVE_MAX_ITEMS);
        return Mono.fromSupplier(() -> 
                taskService.getTasksByStatus(status, pageable).getContent()
        ).subscribeOn(Schedulers.boundedElastic());
    }
    
    @GetMapping("/stats")
    @Operation(
            summary = "Estatísticas em tempo real",
            description = "Retorna estatísticas de tarefas usando programação reativa. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - Token JWT inválido ou ausente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas requisições - Rate limit excedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
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
