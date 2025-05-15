package com.example.queryapi.controller;

import com.example.queryapi.dto.SqlTemplateDto;
import com.example.queryapi.service.SqlTemplateService;
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
@RequestMapping("/api/v1/sql-templates")
@Tag(name = "SQL Template Management", description = "APIs for managing SQL templates")
public class SqlTemplateController {

    private final SqlTemplateService sqlTemplateService;

    @Autowired
    public SqlTemplateController(SqlTemplateService sqlTemplateService) {
        this.sqlTemplateService = sqlTemplateService;
    }

    @PostMapping
    @Operation(summary = "Create a new SQL template",
               responses = {
                   @ApiResponse(responseCode = "201", description = "SQL template created successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SqlTemplateDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input data")
               })
    public ResponseEntity<SqlTemplateDto> createSqlTemplate(@Valid @RequestBody SqlTemplateDto sqlTemplateDto) {
        SqlTemplateDto createdTemplate = sqlTemplateService.createSqlTemplate(sqlTemplateDto);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }

    @GetMapping("/{sqlCode}")
    @Operation(summary = "Get an SQL template by its code",
               responses = {
                   @ApiResponse(responseCode = "200", description = "SQL template found",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SqlTemplateDto.class))),
                   @ApiResponse(responseCode = "404", description = "SQL template not found")
               })
    public ResponseEntity<SqlTemplateDto> getSqlTemplateByCode(@Parameter(description = "Unique code of the SQL template") @PathVariable String sqlCode) {
        return sqlTemplateService.getSqlTemplateBySqlCode(sqlCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all SQL templates",
               responses = {
                   @ApiResponse(responseCode = "200", description = "List of SQL templates",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
               })
    public ResponseEntity<List<SqlTemplateDto>> getAllSqlTemplates() {
        List<SqlTemplateDto> templates = sqlTemplateService.getAllSqlTemplates();
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{sqlCode}")
    @Operation(summary = "Update an existing SQL template",
               responses = {
                   @ApiResponse(responseCode = "200", description = "SQL template updated successfully",
                                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SqlTemplateDto.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid input data"),
                   @ApiResponse(responseCode = "404", description = "SQL template not found")
               })
    public ResponseEntity<SqlTemplateDto> updateSqlTemplate(@Parameter(description = "Unique code of the SQL template to update") @PathVariable String sqlCode,
                                                          @Valid @RequestBody SqlTemplateDto sqlTemplateDto) {
        SqlTemplateDto updatedTemplate = sqlTemplateService.updateSqlTemplate(sqlCode, sqlTemplateDto);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{sqlCode}")
    @Operation(summary = "Delete an SQL template by its code",
               responses = {
                   @ApiResponse(responseCode = "204", description = "SQL template deleted successfully"),
                   @ApiResponse(responseCode = "404", description = "SQL template not found")
               })
    public ResponseEntity<Void> deleteSqlTemplate(@Parameter(description = "Unique code of the SQL template to delete") @PathVariable String sqlCode) {
        sqlTemplateService.deleteSqlTemplate(sqlCode);
        return ResponseEntity.noContent().build();
    }
}

