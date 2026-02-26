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
@Schema(description = "Generic paginated response")
public class PageResponse<T> {
    
    @Schema(description = "List of items in the current page")
    @JsonProperty("content")
    private List<T> content;
    
    @Schema(description = "Total number of pages")
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @Schema(description = "Total number of elements")
    @JsonProperty("totalElements")
    private Long totalElements;
    
    @Schema(description = "Page size")
    @JsonProperty("size")
    private Integer size;
    
    @Schema(description = "Current page number (0-indexed)")
    @JsonProperty("number")
    private Integer number;
    
    @Schema(description = "Number of elements in the current page")
    @JsonProperty("numberOfElements")
    private Integer numberOfElements;
    
    @Schema(description = "Whether this is the first page")
    @JsonProperty("first")
    private Boolean first;
    
    @Schema(description = "Whether this is the last page")
    @JsonProperty("last")
    private Boolean last;
    
    @Schema(description = "Whether the page is empty")
    @JsonProperty("empty")
    private Boolean empty;
}
