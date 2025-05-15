package com.example.queryapi;

import com.example.queryapi.service.SqlTemplateService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(scanBasePackages = "com.example.queryapi")
@MapperScan("com.example.queryapi.dao.mybatis.mapper") // Scan for MyBatis mappers
@EnableAsync // Enable asynchronous processing for @Async if needed, or for CompletableFuture executor
public class QueryApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryApiServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadInitialSqlTemplates(SqlTemplateService sqlTemplateService) {
        return args -> {
            sqlTemplateService.loadInitialTemplates();
        };
    }

    @Bean("batchQueryExecutorService") // Qualifier for the batch query executor
    public ExecutorService batchQueryExecutorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Configure as needed
        executor.setMaxPoolSize(20);  // Configure as needed
        executor.setQueueCapacity(50); // Configure as needed
        executor.setThreadNamePrefix("BatchQueryExec-");
        executor.initialize();
        return executor;
    }
}

