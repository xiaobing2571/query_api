package com.example.queryapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlTemplateDto {

    private Long id;

    @NotBlank(message = "SQL Code cannot be blank")
    @Size(max = 255, message = "SQL Code cannot exceed 255 characters")
    private String sqlCode;

    @NotBlank(message = "SQL Content cannot be blank")
    private String sqlContent;

    private String description;

    private Integer version;

    @Size(max = 50, message = "Data source type hint cannot exceed 50 characters")
    private String dataSourceTypeHint;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
}

