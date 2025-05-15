package com.example.queryapi.service;

import com.example.queryapi.dto.SqlTemplateDto; // Assuming DTOs will be created later
import java.util.List;
import java.util.Optional;

public interface SqlTemplateService {

    /**
     * Creates a new SQL template.
     *
     * @param sqlTemplateDto DTO containing SQL template details.
     * @return The created SQL template DTO.
     */
    SqlTemplateDto createSqlTemplate(SqlTemplateDto sqlTemplateDto);

    /**
     * Retrieves an SQL template by its unique sqlCode.
     *
     * @param sqlCode The unique identifier of the SQL template.
     * @return An Optional containing the SQL template DTO if found, or an empty Optional otherwise.
     */
    Optional<SqlTemplateDto> getSqlTemplateBySqlCode(String sqlCode);

    /**
     * Retrieves all SQL templates.
     *
     * @return A list of all SQL template DTOs.
     */
    List<SqlTemplateDto> getAllSqlTemplates();

    /**
     * Updates an existing SQL template.
     *
     * @param sqlCode The unique identifier of the SQL template to update.
     * @param sqlTemplateDto DTO containing the updated SQL template details.
     * @return The updated SQL template DTO.
     */
    SqlTemplateDto updateSqlTemplate(String sqlCode, SqlTemplateDto sqlTemplateDto);

    /**
     * Deletes an SQL template by its unique sqlCode.
     *
     * @param sqlCode The unique identifier of the SQL template to delete.
     */
    void deleteSqlTemplate(String sqlCode);

    /**
     * Loads SQL templates from resource files into the database during application startup.
     * This method is intended to be called once, typically via an ApplicationRunner or InitializingBean.
     */
    void loadInitialTemplates();
}

