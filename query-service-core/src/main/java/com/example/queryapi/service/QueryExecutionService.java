package com.example.queryapi.service;

import com.example.queryapi.dto.BatchQueryRequestDto;
import com.example.queryapi.dto.BatchQueryResponseDto;
import com.example.queryapi.dto.SingleQueryRequestDto;
import com.example.queryapi.dto.SingleQueryResponseDto;

public interface QueryExecutionService {

    /**
     * Executes a single SQL query based on the provided request.
     *
     * @param requestDto DTO containing details for the single query execution,
     *                   including datasourceId, sqlCode or raw SQL, and parameters.
     * @return DTO containing the result of the single query execution.
     */
    SingleQueryResponseDto executeSingleQuery(SingleQueryRequestDto requestDto);

    /**
     * Executes a batch of SQL queries based on the provided request.
     *
     * @param batchRequestDto DTO containing a list of individual query requests.
     * @return DTO containing results for each query in the batch.
     */
    BatchQueryResponseDto executeBatchQuery(BatchQueryRequestDto batchRequestDto);

}

