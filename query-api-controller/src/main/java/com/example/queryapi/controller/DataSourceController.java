package com.example.queryapi.controller;

import com.example.queryapi.dto.DataSourceDto;
import com.example.queryapi.service.DataSourceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasources")
@Tag(name = "Data Source Management", description = "APIs for managing data source configurations")
public class DataSourceController {

    private final DataSourceManagementService dataSourceManagementService;

    @Autowired
    public DataSourceController(DataSourceManagementService dataSourceManagementService) {
        this.dataSourceManagementService = dataSourceManagementService;
    }

    @PostMapping
    @Operation(summary = "Create a new data source configuration",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Data source created successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataSourceDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input data")
               })
    public ResponseEntity<DataSourceDto> createDataSource(@Valid @RequestBody DataSourceDto dataSourceDto) {
        DataSourceDto createdDataSource = dataSourceManagementService.createDataSource(dataSourceDto);
        return new ResponseEntity<>(createdDataSource, HttpStatus.CREATED);
    }

    @GetMapping("/{datasourceId}")
    @Operation(summary = "Get a data source configuration by its ID",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Data source found",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataSourceDto.class))),
                   @ApiResponse(responseCode = "404", description = "Data source not found")
               })
    public ResponseEntity<DataSourceDto> getDataSourceById(@Parameter(description = "Unique ID of the data source") @PathVariable String datasourceId) {
        return dataSourceManagementService.getDataSourceByDatasourceId(datasourceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all data source configurations",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of data sources",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
               })
    public ResponseEntity<List<DataSourceDto>> getAllDataSources() {
        List<DataSourceDto> dataSources = dataSourceManagementService.getAllDataSources();
        return ResponseEntity.ok(dataSources);
    }

    @PutMapping("/{datasourceId}")
    @Operation(summary = "Update an existing data source configuration",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Data source updated successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataSourceDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input data"),
                   @ApiResponse(responseCode = "404", description = "Data source not found")
               })
    public ResponseEntity<DataSourceDto> updateDataSource(@Parameter(description = "Unique ID of the data source to update") @PathVariable String datasourceId,
                                                      @Valid @RequestBody DataSourceDto dataSourceDto) {
        DataSourceDto updatedDataSource = dataSourceManagementService.updateDataSource(datasourceId, dataSourceDto);
        return ResponseEntity.ok(updatedDataSource);
    }

    @DeleteMapping("/{datasourceId}")
    @Operation(summary = "Delete a data source configuration by its ID",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Data source deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "Data source not found")
               })
    public ResponseEntity<Void> deleteDataSource(@Parameter(description = "Unique ID of the data source to delete") @PathVariable String datasourceId) {
        dataSourceManagementService.deleteDataSource(datasourceId);
        return ResponseEntity.noContent().build();
    }
}

