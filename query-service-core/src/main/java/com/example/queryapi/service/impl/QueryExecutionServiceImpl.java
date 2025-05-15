package com.example.queryapi.service.impl;

import com.example.queryapi.common.exception.InvalidRequestException;
import com.example.queryapi.dao.mybatis.mapper.GenericMapper;
import com.example.queryapi.dto.*;
import com.example.queryapi.service.DataSourceManagementService;
import com.example.queryapi.service.QueryExecutionService;
import com.example.queryapi.service.SqlTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QueryExecutionServiceImpl implements QueryExecutionService {

    private final DataSourceManagementService dataSourceManagementService;
    private final SqlTemplateService sqlTemplateService;
    // private final GenericMapper genericMapper; // This might not be directly usable with dynamic datasources easily
    private final SqlSessionFactory sqlSessionFactory; // Autowire the main one, or handle dynamic ones
    private final ExecutorService batchQueryExecutor; // For parallel execution of batch queries

    @Autowired
    public QueryExecutionServiceImpl(DataSourceManagementService dataSourceManagementService,
                                     SqlTemplateService sqlTemplateService,
                                     SqlSessionFactory sqlSessionFactory, // Default SqlSessionFactory
                                     @Qualifier("batchQueryExecutorService") ExecutorService batchQueryExecutor) {
        this.dataSourceManagementService = dataSourceManagementService;
        this.sqlTemplateService = sqlTemplateService;
        this.sqlSessionFactory = sqlSessionFactory;
        this.batchQueryExecutor = batchQueryExecutor;
    }

    @Override
    public SingleQueryResponseDto executeSingleQuery(SingleQueryRequestDto requestDto) {
        long startTime = System.currentTimeMillis();
        String sqlToExecute;
        String effectiveSqlCode = requestDto.getSqlCode();

        if (requestDto.getSqlCode() != null && !requestDto.getSqlCode().isBlank()) {
            SqlTemplateDto templateDto = sqlTemplateService.getSqlTemplateBySqlCode(requestDto.getSqlCode())
                    .orElseThrow(() -> new InvalidRequestException("SQL Template not found with code: " + requestDto.getSqlCode()));
            sqlToExecute = templateDto.getSqlContent();
        } else if (requestDto.getRawSql() != null && !requestDto.getRawSql().isBlank()) {
            // Potentially add validation/sanitization for raw SQL if allowed
            // For security, raw SQL execution should be highly restricted or disabled by default
            log.warn("Executing raw SQL for datasource: {}. This should be used with extreme caution.", requestDto.getDatasourceId());
            sqlToExecute = requestDto.getRawSql();
            effectiveSqlCode = "raw_sql_" + startTime; // Create a temporary identifier for raw SQL
        } else {
            throw new InvalidRequestException("Either sqlCode or rawSql must be provided.");
        }

        try {
            DataSource targetDataSource = dataSourceManagementService.getActiveDataSource(requestDto.getDatasourceId());
            
            // For dynamic datasources, it's often easier to manually open a SqlSession
            // from a SqlSessionFactory configured for that DataSource, or use a routing DataSource.
            // If using a single SqlSessionFactory that routes, this could be simpler.
            // Assuming a more manual approach for clarity with truly dynamic sources:
            
            // This is a simplified approach. A robust solution would involve a SqlSessionFactory per DataSource
            // or a routing SqlSessionFactory. For now, we'll try to use the generic mapper with the default factory,
            // which implies the default factory's DataSource must be a routing one, or this won't work for multiple DBs.
            // A better way is to get a SqlSession for the specific DataSource.

            List<Map<String, Object>> resultData;
            // The SqlSessionTemplate is typically bound to a single DataSource (the primary one).
            // To work with dynamic DataSources, you need to obtain a SqlSession specifically for that DataSource.
            // One way is to create a new SqlSessionFactory for each dynamic DataSource, or use a routing DataSource
            // that Spring's transaction management and MyBatis can work with.

            // Let's assume sqlSessionFactory.getConfiguration().getEnvironment().getDataSource() is a routing DataSource
            // or we manually manage SqlSession.
            try (SqlSession sqlSession = sqlSessionFactory.openSession(targetDataSource.getConnection())) {
                 // It's generally better to use a specific mapper method if possible.
                 // If using a generic string-based SQL execution, ensure parameters are handled safely.
                 // MyBatis mappers handle parameter substitution safely with #{}.
                 // When passing raw SQL string like this, parameter substitution needs careful handling.
                 // The GenericMapper approach is one way if it's designed to handle parameters correctly.
                
                // The GenericMapper defined earlier takes @Param("sql") and @Param("params").
                // We need to ensure the SqlSession used by this mapper is tied to the targetDataSource.
                // This is where a custom SqlSession or a routing DataSource becomes critical.
                
                // For simplicity in this step, let's assume GenericMapper is obtained from the correct SqlSession.
                // This is a conceptual simplification. Real implementation needs robust DataSource routing for MyBatis.
                GenericMapper mapper = sqlSession.getMapper(GenericMapper.class);
                resultData = mapper.executeSelect(sqlToExecute, requestDto.getParams());
                sqlSession.commit(); // or sqlSession.close(); if no DML
            }

            long endTime = System.currentTimeMillis();
            return SingleQueryResponseDto.success(effectiveSqlCode, resultData, endTime - startTime);
        } catch (Exception e) {
            log.error("Error executing query for sqlCode 	'{}" or raw SQL on datasource 	'{}": {}", 
                requestDto.getSqlCode(), requestDto.getDatasourceId(), e.getMessage(), e);
            long endTime = System.currentTimeMillis();
            // Distinguish between data access errors (FAILURE) and other processing errors (ERROR)
            // For now, mapping all to ERROR for simplicity
            return SingleQueryResponseDto.error(effectiveSqlCode, e.getMessage(), endTime - startTime);
        }
    }

    @Override
    public BatchQueryResponseDto executeBatchQuery(BatchQueryRequestDto batchRequestDto) {
        long batchStartTime = System.currentTimeMillis();
        List<CompletableFuture<SingleQueryResponseDto>> futures = new ArrayList<>();

        for (SingleQueryRequestDto singleRequest : batchRequestDto.getQueries()) {
            // TODO: Consider if sequential execution option is needed.
            // For now, submitting all to executor for parallel processing.
            CompletableFuture<SingleQueryResponseDto> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Need to ensure that if a routing DataSource is used, the context for routing
                    // (e.g., ThreadLocal) is correctly set and cleared for each async task.
                    // This is a common challenge with async processing and routing DataSources.
                    // One solution is to pass the datasourceId and have executeSingleQuery resolve it again,
                    // or ensure the routing mechanism is compatible with CompletableFuture's thread pool.
                    return executeSingleQuery(singleRequest);
                } catch (Exception e) {
                    log.error("Exception in batch execution for one query (sqlCode: {}, datasource: {}): {}", 
                        singleRequest.getSqlCode(), singleRequest.getDatasourceId(), e.getMessage(), e);
                    return SingleQueryResponseDto.error(
                        singleRequest.getSqlCode() != null ? singleRequest.getSqlCode() : "raw_sql_async_error",
                        "Unhandled async execution error: " + e.getMessage(), 0L);
                }
            }, batchQueryExecutor);
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<SingleQueryResponseDto> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get(); // Should not block due to join() above, but handles checked exceptions
                    } catch (Exception e) {
                        // This catch block is for CompletableFuture.get() exceptions, 
                        // which should ideally be handled within the supplyAsync block itself.
                        log.error("Unexpected error retrieving future result in batch: {}", e.getMessage(), e);
                        return SingleQueryResponseDto.error("unknown_sql_code", "Future retrieval error: " + e.getMessage(), 0L);
                    }
                })
                .collect(Collectors.toList());

        long batchEndTime = System.currentTimeMillis();
        return new BatchQueryResponseDto(results, batchEndTime - batchStartTime);
    }
}

