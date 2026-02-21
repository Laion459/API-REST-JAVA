package com.leonardoborges.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Resposta paginada genérica")
public class PageResponse<T> {
    
    @Schema(description = "Lista de itens da página atual")
    @JsonProperty("content")
    private List<T> content;
    
    @Schema(description = "Número total de páginas")
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @Schema(description = "Número total de elementos")
    @JsonProperty("totalElements")
    private Long totalElements;
    
    @Schema(description = "Tamanho da página")
    @JsonProperty("size")
    private Integer size;
    
    @Schema(description = "Número da página atual (0-indexed)")
    @JsonProperty("number")
    private Integer number;
    
    @Schema(description = "Número de elementos na página atual")
    @JsonProperty("numberOfElements")
    private Integer numberOfElements;
    
    @Schema(description = "Se é a primeira página")
    @JsonProperty("first")
    private Boolean first;
    
    @Schema(description = "Se é a última página")
    @JsonProperty("last")
    private Boolean last;
    
    @Schema(description = "Se a página está vazia")
    @JsonProperty("empty")
    private Boolean empty;
}
