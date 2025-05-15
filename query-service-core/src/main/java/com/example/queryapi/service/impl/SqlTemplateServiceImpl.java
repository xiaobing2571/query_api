package com.example.queryapi.service.impl;

import com.example.queryapi.common.exception.ResourceNotFoundException;
import com.example.queryapi.dao.jpa.entity.SqlTemplateEntity;
import com.example.queryapi.dao.jpa.repository.SqlTemplateRepository;
import com.example.queryapi.dto.SqlTemplateDto;
import com.example.queryapi.service.SqlTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SqlTemplateServiceImpl implements SqlTemplateService {

    private final SqlTemplateRepository sqlTemplateRepository;
    private static final String SQL_TEMPLATES_PATH = "classpath*:sql-templates/**/*.sql";

    @Autowired
    public SqlTemplateServiceImpl(SqlTemplateRepository sqlTemplateRepository) {
        this.sqlTemplateRepository = sqlTemplateRepository;
    }

    @Override
    @Transactional
    public SqlTemplateDto createSqlTemplate(SqlTemplateDto sqlTemplateDto) {
        if (sqlTemplateRepository.existsBySqlCode(sqlTemplateDto.getSqlCode())) {
            throw new IllegalArgumentException("SQL Template with code " + sqlTemplateDto.getSqlCode() + " already exists.");
        }
        SqlTemplateEntity entity = convertToEntity(sqlTemplateDto);
        // Set audit fields - createdBy should ideally come from security context
        entity.setCreatedBy("system"); // Placeholder
        entity.setLastModifiedBy("system"); // Placeholder
        SqlTemplateEntity savedEntity = sqlTemplateRepository.save(entity);
        return convertToDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SqlTemplateDto> getSqlTemplateBySqlCode(String sqlCode) {
        return sqlTemplateRepository.findBySqlCode(sqlCode)
                .map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SqlTemplateDto> getAllSqlTemplates() {
        return sqlTemplateRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SqlTemplateDto updateSqlTemplate(String sqlCode, SqlTemplateDto sqlTemplateDto) {
        SqlTemplateEntity existingEntity = sqlTemplateRepository.findBySqlCode(sqlCode)
                .orElseThrow(() -> new ResourceNotFoundException("SQL Template not found with code: " + sqlCode));

        // Update fields from DTO
        existingEntity.setSqlContent(sqlTemplateDto.getSqlContent());
        existingEntity.setDescription(sqlTemplateDto.getDescription());
        existingEntity.setDataSourceTypeHint(sqlTemplateDto.getDataSourceTypeHint());
        existingEntity.setLastModifiedBy("system"); // Placeholder - should be from security context
        // Version will be incremented by JPA if @Version is used correctly

        SqlTemplateEntity updatedEntity = sqlTemplateRepository.save(existingEntity);
        return convertToDto(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteSqlTemplate(String sqlCode) {
        if (!sqlTemplateRepository.existsBySqlCode(sqlCode)) {
            throw new ResourceNotFoundException("SQL Template not found with code: " + sqlCode);
        }
        sqlTemplateRepository.deleteBySqlCode(sqlCode);
        log.info("SQL Template with code {} deleted.", sqlCode);
    }

    @Override
    @Transactional
    public void loadInitialTemplates() {
        log.info("Starting to load initial SQL templates from path: {}", SQL_TEMPLATES_PATH);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(SQL_TEMPLATES_PATH);
            if (resources.length == 0) {
                log.warn("No SQL template files found at path: {}", SQL_TEMPLATES_PATH);
                return;
            }
            for (Resource resource : resources) {
                String sqlCode = resource.getFilename();
                if (sqlCode != null && sqlCode.endsWith(".sql")) {
                    sqlCode = sqlCode.substring(0, sqlCode.lastIndexOf(".sql"));
                }

                if (sqlCode == null || sqlCode.isBlank()) {
                    log.warn("Skipping resource with invalid filename (cannot derive sqlCode): {}", resource.getFilename());
                    continue;
                }

                // Check if template already exists
                if (sqlTemplateRepository.existsBySqlCode(sqlCode)) {
                    log.info("SQL Template with code '{}' already exists in DB, skipping load from file: {}", sqlCode, resource.getFilename());
                    continue;
                }

                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    String sqlContent = FileCopyUtils.copyToString(reader);
                    SqlTemplateEntity templateEntity = new SqlTemplateEntity();
                    templateEntity.setSqlCode(sqlCode);
                    templateEntity.setSqlContent(sqlContent);
                    templateEntity.setDescription("Loaded from file: " + resource.getFilename());
                    templateEntity.setCreatedBy("system-loader");
                    templateEntity.setLastModifiedBy("system-loader");
                    templateEntity.setCreatedAt(LocalDateTime.now());
                    templateEntity.setUpdatedAt(LocalDateTime.now());
                    // Version will be initialized by JPA

                    sqlTemplateRepository.save(templateEntity);
                    log.info("Successfully loaded SQL Template with code '{}' from file: {}", sqlCode, resource.getFilename());
                } catch (IOException e) {
                    log.error("Failed to read SQL template file: {}", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan for SQL template files at path: {}", SQL_TEMPLATES_PATH, e);
        }
        log.info("Finished loading initial SQL templates.");
    }

    private SqlTemplateDto convertToDto(SqlTemplateEntity entity) {
        SqlTemplateDto dto = new SqlTemplateDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private SqlTemplateEntity convertToEntity(SqlTemplateDto dto) {
        SqlTemplateEntity entity = new SqlTemplateEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}

