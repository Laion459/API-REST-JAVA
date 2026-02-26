package com.leonardoborges.api.controller;

import com.leonardoborges.api.exception.ErrorResponse;
import com.leonardoborges.api.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for cache management operations.
 * Provides endpoints for cache statistics and administrative operations.
 */
@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cache Management", description = "API for cache management and statistics")
public class CacheController {
    
    private final CacheService cacheService;
    
    @GetMapping("/stats")
    @Operation(
            summary = "Get cache statistics",
            description = "Returns information about cache configuration and status. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cache statistics returned successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Map<String, String>> getCacheStats() {
        log.debug("Fetching cache statistics");
        String stats = cacheService.getCacheStatistics();
        return ResponseEntity.ok(Map.of("statistics", stats));
    }
    
    @GetMapping("/tasks/{id}/cached")
    @Operation(
            summary = "Check if task is cached",
            description = "Returns whether a specific task is currently cached. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cache status returned successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Map<String, Boolean>> isTaskCached(@PathVariable Long id) {
        log.debug("Checking if task {} is cached", id);
        boolean cached = cacheService.isTaskCached(id);
        return ResponseEntity.ok(Map.of("cached", cached));
    }
    
    @DeleteMapping("/tasks/{id}")
    @Operation(
            summary = "Remove task from cache",
            description = "Manually removes a specific task from cache. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Task removed from cache successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> evictTask(@PathVariable Long id) {
        log.info("Manually evicting task {} from cache", id);
        cacheService.evictTask(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/stats")
    @Operation(
            summary = "Clear statistics cache",
            description = "Removes all task statistics from cache. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Statistics cache cleared successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> clearStatsCache() {
        log.info("Clearing task statistics cache");
        cacheService.evictAllTaskStats();
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Clear all caches",
            description = "Administrative operation to clear all caches. Use with caution. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Todos os caches limpos com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated - Invalid or missing JWT token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado - Requer role ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests - Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> clearAllCaches() {
        log.warn("Clearing all caches - administrative operation");
        cacheService.clearAllCaches();
        return ResponseEntity.noContent().build();
    }
}
