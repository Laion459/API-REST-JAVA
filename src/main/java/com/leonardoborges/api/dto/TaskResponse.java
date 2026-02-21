package com.leonardoborges.api.dto;

import com.leonardoborges.api.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO com informações da tarefa")
public class TaskResponse {
    
    @Schema(description = "ID único da tarefa", example = "1")
    private Long id;
    
    @Schema(description = "Título da tarefa", example = "Implementar feature X")
    private String title;
    
    @Schema(description = "Descrição detalhada da tarefa", example = "Implementar nova funcionalidade com testes")
    private String description;
    
    @Schema(description = "Status atual da tarefa", example = "PENDING")
    private Task.TaskStatus status;
    
    @Schema(description = "Prioridade da tarefa", example = "1")
    private Integer priority;
    
    @Schema(description = "Versão da tarefa para optimistic locking", example = "1")
    private Long version;
    
    @Schema(description = "Data de criação da tarefa", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2025-01-15T14:45:00")
    private LocalDateTime updatedAt;
}
