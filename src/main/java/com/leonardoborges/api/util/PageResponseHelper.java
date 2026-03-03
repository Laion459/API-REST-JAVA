package com.leonardoborges.api.util;

import com.leonardoborges.api.dto.TaskPageResponse;
import com.leonardoborges.api.dto.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

public final class PageResponseHelper {
    
    private PageResponseHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static TaskPageResponse buildTaskPageResponse(Page<TaskResponse> page) {
        return TaskPageResponse.builder()
                .content(new ArrayList<>(page.getContent()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
