package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskPageResponse;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.service.ReactiveTaskService;
import com.leonardoborges.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller reativo para operações de leitura de alta performance.
 * Usa WebFlux para melhor escalabilidade e uso de recursos.
 * 
 * Endpoints reativos otimizados para:
 * - Alta concorrência (milhares de requisições simultâneas)
 * - Baixa latência (operações não-bloqueantes)
 * - Melhor uso de recursos (event loop vs thread pool)
 * 
 * Para operações de escrita (POST/PUT/DELETE), use TaskController (MVC).
 */
@RestController
@RequestMapping("/api/v2/reactive/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reactive Tasks", description = "Endpoints reativos de alta performance para leitura de tarefas (WebFlux)")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.r2dbc.enabled", havingValue = "true", matchIfMissing = false)
public class ReactiveTaskController {
    
    private final ReactiveTaskService reactiveTaskService;
    private final SecurityUtils securityUtils;
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar tarefa por ID (Reativo)",
            description = "Retorna uma tarefa específica usando programação reativa (WebFlux) para alta performance. " +
                    "Otimizado para alta concorrência e baixa latência. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskResponse>> getTaskById(
            @Parameter(description = "ID da tarefa") @PathVariable Long id) {
        log.debug("GET /api/v2/reactive/tasks/{} - Fetching task reactively", id);
        Long userId = securityUtils.getCurrentUser().getId();
        return reactiveTaskService.getTaskById(id, userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
            summary = "Listar todas as tarefas (Reativo)",
            description = "Retorna lista paginada de tarefas usando programação reativa para alta performance. " +
                    "Suporta milhares de requisições concorrentes. Requer autenticação JWT.\n\n" +
                    "**Parâmetros:**\n" +
                    "- `page`: Número da página (padrão: 0)\n" +
                    "- `size`: Tamanho da página (padrão: 20)\n\n" +
                    "**Performance:** Otimizado para alta concorrência usando event loop não-bloqueante.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarefas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskPageResponse>> getAllTasks(
            @Parameter(description = "Número da página (padrão: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (padrão: 20)") 
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v2/reactive/tasks - Fetching all tasks reactively: page={}, size={}", page, size);
        Long userId = securityUtils.getCurrentUser().getId();
        
        return reactiveTaskService.getAllTasks(page, size, userId)
                .collectList()
                .map(tasks -> {
                    TaskPageResponse response = TaskPageResponse.builder()
                            .content(tasks)
                            .size(size)
                            .number(page)
                            .numberOfElements(tasks.size())
                            .first(page == 0)
                            .last(tasks.size() < size)
                            .empty(tasks.isEmpty())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }
    
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Listar tarefas por status (Reativo)",
            description = "Retorna lista paginada de tarefas filtradas por status usando programação reativa. " +
                    "Otimizado para alta performance. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarefas filtradas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<TaskPageResponse>> getTasksByStatus(
            @Parameter(description = "Status da tarefa (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)") 
            @PathVariable Task.TaskStatus status,
            @Parameter(description = "Número da página (padrão: 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (padrão: 20)") 
            @RequestParam(defaultValue = "20") int size) {
        log.debug("GET /api/v2/reactive/tasks/status/{} - Fetching tasks by status reactively", status);
        Long userId = securityUtils.getCurrentUser().getId();
        
        return reactiveTaskService.getTasksByStatus(status, page, size, userId)
                .collectList()
                .map(tasks -> {
                    TaskPageResponse response = TaskPageResponse.builder()
                            .content(tasks)
                            .size(size)
                            .number(page)
                            .numberOfElements(tasks.size())
                            .first(page == 0)
                            .last(tasks.size() < size)
                            .empty(tasks.isEmpty())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }
    
    @GetMapping("/stats/count")
    @Operation(
            summary = "Estatísticas de tarefas (Reativo)",
            description = "Retorna contagem de tarefas por status usando programação reativa. " +
                    "Otimizado para leitura rápida. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Mono<ResponseEntity<Map<String, Long>>> getTaskStats() {
        log.debug("GET /api/v2/reactive/tasks/stats/count - Fetching task statistics reactively");
        Long userId = securityUtils.getCurrentUser().getId();
        return reactiveTaskService.getTaskStats(userId)
                .map(ResponseEntity::ok);
    }
}
