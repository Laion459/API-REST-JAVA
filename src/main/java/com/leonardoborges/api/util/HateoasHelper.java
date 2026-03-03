package com.leonardoborges.api.util;

import com.leonardoborges.api.dto.Link;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper utility for building HATEOAS links.
 * Enables hypermedia-driven API navigation.
 */
public final class HateoasHelper {
    
    private static final String API_BASE_PATH = "/api/v1";
    
    private HateoasHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Builds self link for a task resource.
     */
    public static Link buildTaskSelfLink(@NonNull Long taskId) {
        return Link.builder()
                .rel("self")
                .href(API_BASE_PATH + "/tasks/" + taskId)
                .method("GET")
                .description("Get this task")
                .build();
    }
    
    /**
     * Builds update link for a task resource.
     */
    public static Link buildTaskUpdateLink(@NonNull Long taskId) {
        return Link.builder()
                .rel("update")
                .href(API_BASE_PATH + "/tasks/" + taskId)
                .method("PUT")
                .description("Update this task")
                .build();
    }
    
    /**
     * Builds delete link for a task resource.
     */
    public static Link buildTaskDeleteLink(@NonNull Long taskId) {
        return Link.builder()
                .rel("delete")
                .href(API_BASE_PATH + "/tasks/" + taskId)
                .method("DELETE")
                .description("Delete this task")
                .build();
    }
    
    /**
     * Builds patch link for a task resource.
     */
    public static Link buildTaskPatchLink(@NonNull Long taskId) {
        return Link.builder()
                .rel("patch")
                .href(API_BASE_PATH + "/tasks/" + taskId)
                .method("PATCH")
                .description("Partially update this task")
                .build();
    }
    
    /**
     * Builds history link for a task resource.
     */
    public static Link buildTaskHistoryLink(@NonNull Long taskId) {
        return Link.builder()
                .rel("history")
                .href(API_BASE_PATH + "/tasks/" + taskId + "/history")
                .method("GET")
                .description("Get task change history")
                .build();
    }
    
    /**
     * Builds pagination links for a page.
     */
    public static List<Link> buildPaginationLinks(@NonNull Page<?> page, @NonNull String basePath) {
        List<Link> links = new ArrayList<>();
        
        if (page.hasPrevious()) {
            links.add(Link.builder()
                    .rel("prev")
                    .href(basePath + "?page=" + (page.getNumber() - 1) + "&size=" + page.getSize())
                    .method("GET")
                    .description("Previous page")
                    .build());
        }
        
        links.add(Link.builder()
                .rel("self")
                .href(basePath + "?page=" + page.getNumber() + "&size=" + page.getSize())
                .method("GET")
                .description("Current page")
                .build());
        
        if (page.hasNext()) {
            links.add(Link.builder()
                    .rel("next")
                    .href(basePath + "?page=" + (page.getNumber() + 1) + "&size=" + page.getSize())
                    .method("GET")
                    .description("Next page")
                    .build());
        }
        
        links.add(Link.builder()
                .rel("first")
                .href(basePath + "?page=0&size=" + page.getSize())
                .method("GET")
                .description("First page")
                .build());
        
        links.add(Link.builder()
                .rel("last")
                .href(basePath + "?page=" + (page.getTotalPages() - 1) + "&size=" + page.getSize())
                .method("GET")
                .description("Last page")
                .build());
        
        return links;
    }
    
    /**
     * Builds collection links for task list.
     */
    public static List<Link> buildTaskCollectionLinks() {
        List<Link> links = new ArrayList<>();
        
        links.add(Link.builder()
                .rel("self")
                .href(API_BASE_PATH + "/tasks")
                .method("GET")
                .description("List all tasks")
                .build());
        
        links.add(Link.builder()
                .rel("create")
                .href(API_BASE_PATH + "/tasks")
                .method("POST")
                .description("Create a new task")
                .build());
        
        return links;
    }
}
