package com.leonardoborges.api.dto;

import com.leonardoborges.api.constants.TaskConstants;
import com.leonardoborges.api.model.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO para criação ou atualização de tarefa")
public class TaskRequest {
    
    @Schema(description = "Título da tarefa", example = "Implementar feature X", minLength = 3, maxLength = 255)
    @NotBlank(message = "Title is required")
    @Size(min = TaskConstants.TITLE_MIN_LENGTH, max = TaskConstants.TITLE_MAX_LENGTH, 
          message = "Title must be between " + TaskConstants.TITLE_MIN_LENGTH + " and " + TaskConstants.TITLE_MAX_LENGTH + " characters")
    private String title;
    
    @Schema(description = "Descrição detalhada da tarefa", example = "Implementar nova funcionalidade com testes", maxLength = 1000)
    @Size(max = TaskConstants.DESCRIPTION_MAX_LENGTH, 
          message = "Description must not exceed " + TaskConstants.DESCRIPTION_MAX_LENGTH + " characters")
    private String description;
    
    @Schema(description = "Status da tarefa", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private Task.TaskStatus status;
    
    @Schema(description = "Prioridade da tarefa (quanto maior, mais prioritária)", example = "1", minimum = "0")
    private Integer priority;
    
    @Schema(description = "Versão da tarefa para optimistic locking (opcional)", example = "1")
    private Long version; // For optimistic locking
}
