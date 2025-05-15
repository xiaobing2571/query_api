package com.example.queryapi.controller;

import com.example.queryapi.dto.BatchQueryRequestDto;
import com.example.queryapi.dto.BatchQueryResponseDto;
import com.example.queryapi.dto.SingleQueryRequestDto;
import com.example.queryapi.dto.SingleQueryResponseDto;
import com.example.queryapi.service.QueryExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/query")
@Tag(name = "Query Execution", description = "APIs for executing SQL queries")
public class QueryExecutionController {

    private final QueryExecutionService queryExecutionService;

    @Autowired
    public QueryExecutionController(QueryExecutionService queryExecutionService) {
        this.queryExecutionService = queryExecutionService;
    }

    @PostMapping("/execute-single")
    @Operation(summary = "Execute a single SQL query",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Query executed, result returned",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleQueryResponseDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid query request")
               })
    public ResponseEntity<SingleQueryResponseDto> executeSingleQuery(@Valid @RequestBody SingleQueryRequestDto requestDto) {
        SingleQueryResponseDto responseDto = queryExecutionService.executeSingleQuery(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/execute-batch")
    @Operation(summary = "Execute a batch of SQL queries",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Batch query executed, results returned",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BatchQueryResponseDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid batch query request")
               })
    public ResponseEntity<BatchQueryResponseDto> executeBatchQuery(@Valid @RequestBody BatchQueryRequestDto batchRequestDto) {
        BatchQueryResponseDto responseDto = queryExecutionService.executeBatchQuery(batchRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}

