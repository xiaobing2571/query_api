package com.example.queryapi.service.impl;

import com.example.queryapi.common.enums.DataSourceType;
import com.example.queryapi.common.exception.ResourceNotFoundException;
import com.example.queryapi.dao.jpa.entity.DataSourceConfigEntity;
import com.example.queryapi.dao.jpa.repository.DataSourceConfigRepository;
import com.example.queryapi.dto.DataSourceDto;
import com.example.queryapi.service.DataSourceManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataSourceManagementServiceImpl implements DataSourceManagementService {

    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final ObjectMapper objectMapper; // For parsing JSON connectionPoolConfig

    // Cache for active HikariDataSources
    private final Map<String, DataSource> activeDataSources = new ConcurrentHashMap<>();

    @Autowired
    public DataSourceManagementServiceImpl(DataSourceConfigRepository dataSourceConfigRepository, ObjectMapper objectMapper) {
        this.dataSourceConfigRepository = dataSourceConfigRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public DataSourceDto createDataSource(DataSourceDto dataSourceDto) {
        if (dataSourceConfigRepository.existsByDatasourceId(dataSourceDto.getDatasourceId())) {
            throw new IllegalArgumentException("Data source with ID " + dataSourceDto.getDatasourceId() + " already exists.");
        }
        DataSourceConfigEntity entity = convertToEntity(dataSourceDto);
        // Securely handle password if provided - e.g., encrypt before saving
        // For now, assuming it's handled or will be added
        DataSourceConfigEntity savedEntity = dataSourceConfigRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceDto> getDataSourceByDatasourceId(String datasourceId) {
        return dataSourceConfigRepository.findByDatasourceId(datasourceId)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceDto> getAllDataSources() {
        return dataSourceConfigRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DataSourceDto updateDataSource(String datasourceId, DataSourceDto dataSourceDto) {
        DataSourceConfigEntity existingEntity = dataSourceConfigRepository.findByDatasourceId(datasourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Data source not found with ID: " + datasourceId));

        // Update fields from DTO
        BeanUtils.copyProperties(dataSourceDto, existingEntity, "id", "datasourceId", "createdAt");
        // Re-apply datasourceId to ensure it's not changed if DTO had a different one by mistake
        existingEntity.setDatasourceId(datasourceId);

        DataSourceConfigEntity updatedEntity = dataSourceConfigRepository.save(existingEntity);
        activeDataSources.remove(datasourceId); // Invalidate cache
        return convertToDto(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteDataSource(String datasourceId) {
        if (!dataSourceConfigRepository.existsByDatasourceId(datasourceId)) {
            throw new ResourceNotFoundException("Data source not found with ID: " + datasourceId);
        }
        dataSourceConfigRepository.deleteByDatasourceId(datasourceId);
        activeDataSources.remove(datasourceId); // Invalidate cache
        log.info("Data source configuration with ID {} deleted.", datasourceId);
    }

    @Override
    public DataSource getActiveDataSource(String datasourceId) {
        return activeDataSources.computeIfAbsent(datasourceId, id -> {
            log.info("Attempting to create or retrieve active data source for ID: {}", id);
            DataSourceConfigEntity configEntity = dataSourceConfigRepository.findByDatasourceId(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Data source configuration not found for ID: " + id));
            return createHikariDataSource(configEntity);
        });
    }

    private HikariDataSource createHikariDataSource(DataSourceConfigEntity configEntity) {
        HikariConfig hikariConfig = new HikariConfig();

        if (configEntity.getSourceType() == DataSourceType.API_PROVIDED) {
            // TODO: Implement logic to fetch connection details via external API
            // This would involve calling an external service using Feign/WebClient
            // and then populating hikariConfig.setJdbcUrl(), setUsername(), setPassword()
            // For now, this path will likely fail or require placeholder logic.
            log.warn("API_PROVIDED data source type is not fully implemented for dynamic connection fetching. Datasource: {}", configEntity.getDatasourceId());
            // Placeholder - assuming details might be in configEntity for now for this example
            if (configEntity.getJdbcUrl() == null || configEntity.getJdbcUrl().isBlank()) {
                 throw new IllegalStateException("JDBC URL is required for API_PROVIDED data source if not fetched dynamically: " + configEntity.getDatasourceId());
            }
        }

        hikariConfig.setPoolName("HikariPool-" + configEntity.getDatasourceId());
        hikariConfig.setJdbcUrl(configEntity.getJdbcUrl());
        hikariConfig.setUsername(configEntity.getUsername());
        hikariConfig.setPassword(configEntity.getPassword()); // Password should be decrypted if stored encrypted
        // hikariConfig.setDriverClassName(); // Usually not needed with modern JDBC drivers if URL is correct

        // Apply connection pool configuration from JSON
        if (configEntity.getConnectionPoolConfig() != null && !configEntity.getConnectionPoolConfig().isBlank()) {
            try {
                Map<String, String> poolProps = objectMapper.readValue(configEntity.getConnectionPoolConfig(), new TypeReference<Map<String, String>>() {});
                if (poolProps.containsKey("maximumPoolSize")) hikariConfig.setMaximumPoolSize(Integer.parseInt(poolProps.get("maximumPoolSize")));
                if (poolProps.containsKey("minimumIdle")) hikariConfig.setMinimumIdle(Integer.parseInt(poolProps.get("minimumIdle")));
                if (poolProps.containsKey("connectionTimeout")) hikariConfig.setConnectionTimeout(Long.parseLong(poolProps.get("connectionTimeout")));
                if (poolProps.containsKey("idleTimeout")) hikariConfig.setIdleTimeout(Long.parseLong(poolProps.get("idleTimeout")));
                if (poolProps.containsKey("maxLifetime")) hikariConfig.setMaxLifetime(Long.parseLong(poolProps.get("maxLifetime")));
                // Add other HikariCP properties as needed
            } catch (JsonProcessingException e) {
                log.error("Failed to parse connectionPoolConfig JSON for datasource {}: {}", configEntity.getDatasourceId(), configEntity.getConnectionPoolConfig(), e);
                throw new IllegalArgumentException("Invalid connectionPoolConfig JSON format for datasource " + configEntity.getDatasourceId(), e);
            }
        } else {
            // Default sensible values if not configured
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000); // 30 seconds
            hikariConfig.setIdleTimeout(600000); // 10 minutes
            hikariConfig.setMaxLifetime(1800000); // 30 minutes
        }

        log.info("Creating HikariDataSource for datasourceId: {} with URL: {}", configEntity.getDatasourceId(), configEntity.getJdbcUrl());
        try {
            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            log.error("Failed to create HikariDataSource for {}: {}", configEntity.getDatasourceId(), e.getMessage(), e);
            throw new IllegalStateException("Could not create data source for ID: " + configEntity.getDatasourceId(), e);
        }
    }

    private DataSourceDto convertToDto(DataSourceConfigEntity entity) {
        DataSourceDto dto = new DataSourceDto();
        BeanUtils.copyProperties(entity, dto, "password"); // Exclude password from DTO by default
        if (entity.getConnectionPoolConfig() != null && !entity.getConnectionPoolConfig().isBlank()) {
            try {
                dto.setConnectionPoolConfig(objectMapper.readValue(entity.getConnectionPoolConfig(), new TypeReference<Map<String, String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("Could not parse connectionPoolConfig for DTO mapping, datasourceId: {}", entity.getDatasourceId(), e);
                // Potentially set to null or an error indicator in DTO
            }
        }
        return dto;
    }

    private DataSourceConfigEntity convertToEntity(DataSourceDto dto) {
        DataSourceConfigEntity entity = new DataSourceConfigEntity();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getConnectionPoolConfig() != null && !dto.getConnectionPoolConfig().isEmpty()) {
            try {
                entity.setConnectionPoolConfig(objectMapper.writeValueAsString(dto.getConnectionPoolConfig()));
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize connectionPoolConfig for entity mapping, datasourceId: {}", dto.getDatasourceId(), e);
                // Potentially set to null or handle error
            }
        }
        // Password from DTO should be handled here (e.g., encryption)
        // entity.setPassword(encrypt(dto.getPassword()));
        return entity;
    }
}

