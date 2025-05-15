package com.example.queryapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleQueryRequestDto {

    @NotBlank(message = "Datasource ID cannot be blank")
    private String datasourceId;

    // Either sqlCode or rawSql must be provided
    private String sqlCode; // Identifier for a pre-defined SQL template

    private String rawSql; // Raw SQL string for ad-hoc queries (use with caution)

    private Map<String, Object> params; // Parameters for the SQL query

    // Optional: Add a flag to indicate if this is part of a batch or a standalone single query
    // boolean isBatchPart = false;
}

