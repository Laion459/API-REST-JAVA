package com.leonardoborges.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HATEOAS Link representation.
 * Enables hypermedia-driven API navigation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "HATEOAS link for resource navigation")
public class Link {
    
    @Schema(description = "Link relation type", example = "self")
    private String rel;
    
    @Schema(description = "Link URL", example = "/api/v1/tasks/1")
    private String href;
    
    @Schema(description = "HTTP method", example = "GET")
    private String method;
    
    @Schema(description = "Link description", example = "Get this task")
    private String description;
}
