package com.leonardoborges.api.config;

import com.leonardoborges.api.constants.TaskConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TaskConstants.ASYNC_CORE_POOL_SIZE);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(TaskConstants.ASYNC_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}
