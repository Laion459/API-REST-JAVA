package com.leonardoborges.api.controller;

import com.leonardoborges.api.dto.TaskRequest;
import com.leonardoborges.api.dto.TaskResponse;
import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.service.BatchTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para operações em lote (batch) de tasks.
 * Otimizado para processar múltiplas operações de forma eficiente.
 */
@RestController
@RequestMapping("/api/v1/tasks/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch Tasks", description = "Operações em lote para alta performance")
public class BatchTaskController {
    
    private final BatchTaskService batchTaskService;
    
    @Data
    public static class BatchCreateRequest {
        @NotEmpty(message = "Tasks list cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private List<@Valid TaskRequest> tasks;
    }
    
    @Data
    public static class BatchUpdateRequest {
        @NotEmpty(message = "Updates map cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private Map<Long, @Valid TaskRequest> updates;
    }
    
    @Data
    public static class BatchDeleteRequest {
        @NotEmpty(message = "Task IDs list cannot be empty")
        @Size(max = 100, message = "Batch size cannot exceed 100 tasks")
        private List<Long> taskIds;
    }
    
    @PostMapping("/create")
    @Operation(
            summary = "Criar múltiplas tasks em lote",
            description = "Cria múltiplas tasks em uma única transação. Máximo de 100 tasks por lote. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tasks criadas com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou batch muito grande",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<TaskResponse>> createBatch(@Valid @RequestBody BatchCreateRequest request) {
        log.info("POST /api/v1/tasks/batch/create - Creating batch of {} tasks", request.getTasks().size());
        List<TaskResponse> responses = batchTaskService.createBatch(request.getTasks());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @PutMapping("/update")
    @Operation(
            summary = "Atualizar múltiplas tasks em lote",
            description = "Atualiza múltiplas tasks em uma única transação. Máximo de 100 tasks por lote. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou batch muito grande",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<TaskResponse>> updateBatch(@Valid @RequestBody BatchUpdateRequest request) {
        log.info("PUT /api/v1/tasks/batch/update - Updating batch of {} tasks", request.getUpdates().size());
        List<TaskResponse> responses = batchTaskService.updateBatch(request.getUpdates());
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/delete")
    @Operation(
            summary = "Deletar múltiplas tasks em lote",
            description = "Deleta múltiplas tasks (soft delete) em uma única transação. Máximo de 100 tasks por lote. Requer autenticação JWT.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Tasks deletadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou batch muito grande",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBatch(@Valid @RequestBody BatchDeleteRequest request) {
        log.info("DELETE /api/v1/tasks/batch/delete - Deleting batch of {} tasks", request.getTaskIds().size());
        batchTaskService.deleteBatch(request.getTaskIds());
    }
}
