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
@Schema(description = "Paginated task response")
public class TaskPageResponse {
    
    @Schema(description = "List of tasks in the current page")
    private List<TaskResponse> content;
    
    @Schema(description = "Total number of pages")
    private Integer totalPages;
    
    @Schema(description = "Total number of elements")
    private Long totalElements;
    
    @Schema(description = "Page size")
    private Integer size;
    
    @Schema(description = "Current page number (0-indexed)")
    private Integer number;
    
    @Schema(description = "Number of elements in the current page")
    private Integer numberOfElements;
    
    @Schema(description = "Whether this is the first page")
    private Boolean first;
    
    @Schema(description = "Whether this is the last page")
    private Boolean last;
    
    @Schema(description = "Whether the page is empty")
    private Boolean empty;
}
