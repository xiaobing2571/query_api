package com.example.queryapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchQueryResponseDto {

    private List<SingleQueryResponseDto> results;
    private long totalExecutionTimeMillis; // Total time for the entire batch

    // Could add overall status if needed, e.g., ALL_SUCCESS, PARTIAL_SUCCESS, ALL_FAILURE
}

