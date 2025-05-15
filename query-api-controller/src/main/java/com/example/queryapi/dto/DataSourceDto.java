package com.example.queryapi.dto;

import com.example.queryapi.common.enums.DataSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto {

    private Long id;

    @NotBlank(message = "Datasource ID cannot be blank")
    @Size(max = 255, message = "Datasource ID cannot exceed 255 characters")
    private String datasourceId;

    @NotNull(message = "Source type cannot be null")
    private DataSourceType sourceType;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Database type cannot be blank")
    @Size(max = 50, message = "Database type cannot exceed 50 characters")
    private String dbType; // e.g., "MySQL", "PostgreSQL", "Oracle"

    // jdbcUrl, username, password, credentialsKey can be optional depending on sourceType
    // and how they are managed (e.g., fetched via API_PROVIDED)
    @Size(max = 1024, message = "JDBC URL cannot exceed 1024 characters")
    private String jdbcUrl;

    @Size(max = 255, message = "Username cannot exceed 255 characters")
    private String username;

    // Password should not be directly exposed in DTOs for responses.
    // For requests, it might be present but handled securely.
    // Consider separate DTOs for create/update requests if password handling is complex.
    private String password; // Mask or omit in responses

    @Size(max = 1024, message = "Credentials key cannot exceed 1024 characters")
    private String credentialsKey; // For API_PROVIDED type

    // Using Map for flexibility, can be a specific class if structure is fixed
    private Map<String, String> connectionPoolConfig; // e.g., {"maxPoolSize": "10", "minIdle": "2"}

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

