package com.example.queryapi.service;

import com.example.queryapi.dto.DataSourceDto; // Assuming DTOs will be created later
import java.util.List;
import java.util.Optional;

public interface DataSourceManagementService {

    /**
     * Creates a new data source configuration.
     *
     * @param dataSourceDto DTO containing data source configuration details.
     * @return The created data source configuration DTO.
     */
    DataSourceDto createDataSource(DataSourceDto dataSourceDto);

    /**
     * Retrieves a data source configuration by its unique datasourceId.
     *
     * @param datasourceId The unique identifier of the data source.
     * @return An Optional containing the data source DTO if found, or an empty Optional otherwise.
     */
    Optional<DataSourceDto> getDataSourceByDatasourceId(String datasourceId);

    /**
     * Retrieves all configured data sources.
     *
     * @return A list of all data source configuration DTOs.
     */
    List<DataSourceDto> getAllDataSources();

    /**
     * Updates an existing data source configuration.
     *
     * @param datasourceId The unique identifier of the data source to update.
     * @param dataSourceDto DTO containing the updated data source configuration details.
     * @return The updated data source configuration DTO.
     */
    DataSourceDto updateDataSource(String datasourceId, DataSourceDto dataSourceDto);

    /**
     * Deletes a data source configuration by its unique datasourceId.
     *
     * @param datasourceId The unique identifier of the data source to delete.
     */
    void deleteDataSource(String datasourceId);

    /**
     * Gets the actual javax.sql.DataSource object for a given datasourceId.
     * This method will handle dynamic creation and pooling.
     *
     * @param datasourceId The unique identifier of the data source.
     * @return The configured javax.sql.DataSource.
     * @throws com.example.queryapi.common.exception.ResourceNotFoundException if the datasourceId is not found or cannot be configured.
     */
    javax.sql.DataSource getActiveDataSource(String datasourceId);
}

