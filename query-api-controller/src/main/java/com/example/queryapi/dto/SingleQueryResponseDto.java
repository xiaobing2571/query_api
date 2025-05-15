package com.example.queryapi.dto;

import com.example.queryapi.common.enums.ExecutionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleQueryResponseDto {

    private String sqlCode; // Echo back the sqlCode if provided in request
    private ExecutionStatus status;
    private List<Map<String, Object>> data; // Query result data
    private String errorMessage; // Error message if status is FAILURE or ERROR
    private Long executionTimeMillis; // Optional: execution time

    // Static factory methods for convenience
    public static SingleQueryResponseDto success(String sqlCode, List<Map<String, Object>> data, long executionTimeMillis) {
        return new SingleQueryResponseDto(sqlCode, ExecutionStatus.SUCCESS, data, null, executionTimeMillis);
    }

    public static SingleQueryResponseDto failure(String sqlCode, String errorMessage, long executionTimeMillis) {
        return new SingleQueryResponseDto(sqlCode, ExecutionStatus.FAILURE, null, errorMessage, executionTimeMillis);
    }

    public static SingleQueryResponseDto error(String sqlCode, String errorMessage, long executionTimeMillis) {
        return new SingleQueryResponseDto(sqlCode, ExecutionStatus.ERROR, null, errorMessage, executionTimeMillis);
    }
}

