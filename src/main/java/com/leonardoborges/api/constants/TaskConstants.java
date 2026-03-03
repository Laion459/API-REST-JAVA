package com.leonardoborges.api.constants;

/**
 * Constants for Task-related values.
 * Centralizes magic numbers and configuration values.
 */
public final class TaskConstants {
    
    private TaskConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Validation constraints
    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;
    public static final int DEFAULT_PRIORITY = 0;
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int CACHE_WARMING_PAGE_SIZE = 20;
    public static final int REACTIVE_STREAM_PAGE_SIZE = 10;
    public static final int REACTIVE_MAX_ITEMS = 100;
    
    // Cache TTL (in minutes)
    public static final int CACHE_TTL_DEFAULT_MINUTES = 10;
    public static final int CACHE_TTL_TASKS_MINUTES = 15;
    public static final int CACHE_TTL_STATS_MINUTES = 5;
    
    // Async configuration
    public static final int ASYNC_CORE_POOL_SIZE = 10;
    public static final int ASYNC_QUEUE_CAPACITY = 100;
    
    // Cache names
    public static final String CACHE_NAME_TASKS = "tasks";
    public static final String CACHE_NAME_TASK_STATS = "taskStats";
    public static final String CACHE_NAME_TASK_LISTS = "taskLists";
    
    // Cache key prefixes
    public static final String CACHE_KEY_PREFIX_ALL = "all-";
    public static final String CACHE_KEY_PREFIX_STATUS = "status-";
    
    // Rate limiting
    public static final int RATE_LIMIT_REQUESTS_PER_MINUTE = 60;
    public static final int RATE_LIMIT_REQUESTS_PER_HOUR = 1000;
    public static final int RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE = 5;
    public static final int RATE_LIMIT_ADMIN_REQUESTS_PER_MINUTE = 200;
    
    // Security constants
    public static final int MIN_TOKEN_EXPIRATION_TIME_MS = 0;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long ACCOUNT_LOCKOUT_DURATION_MINUTES = 30;
    
    // JWT constants
    public static final int MIN_JWT_SECRET_LENGTH = 32;
    public static final int RECOMMENDED_JWT_SECRET_LENGTH = 64;
}
