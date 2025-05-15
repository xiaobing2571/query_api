package com.example.queryapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchQueryRequestDto {

    @NotEmpty(message = "Query requests list cannot be empty")
    @Size(max = 50, message = "Batch size cannot exceed 50 queries") // Example limit
    private List<@Valid SingleQueryRequestDto> queries;

    // Optional: Add a flag for sequential or parallel execution if needed
    // private boolean sequentialExecution = false;
}

