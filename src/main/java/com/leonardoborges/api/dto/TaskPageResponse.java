package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada de tarefas")
public class TaskPageResponse {
    
    @Schema(description = "Lista de tarefas da página atual")
    private List<TaskResponse> content;
    
    @Schema(description = "Número total de páginas")
    private Integer totalPages;
    
    @Schema(description = "Número total de elementos")
    private Long totalElements;
    
    @Schema(description = "Tamanho da página")
    private Integer size;
    
    @Schema(description = "Número da página atual (0-indexed)")
    private Integer number;
    
    @Schema(description = "Número de elementos na página atual")
    private Integer numberOfElements;
    
    @Schema(description = "Se é a primeira página")
    private Boolean first;
    
    @Schema(description = "Se é a última página")
    private Boolean last;
    
    @Schema(description = "Se a página está vazia")
    private Boolean empty;
}
