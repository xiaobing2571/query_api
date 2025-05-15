package com.example.queryapi.dao.jpa.entity;

import com.example.queryapi.common.enums.DataSourceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_source_configs", indexes = {
        @Index(name = "idx_datasource_id_unique", columnList = "datasourceId", unique = true)
})
@Data
@EqualsAndHashCode(of = "id") // Base equals/hashCode on ID for JPA entities
public class DataSourceConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String datasourceId; // Business Key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataSourceType sourceType;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private String dbType; // e.g., "MySQL", "PostgreSQL", "Oracle"

    @Lob
    @Column(nullable = true) // Nullable if sourceType is API_PROVIDED and URL comes from there
    private String jdbcUrl; // Consider encryption for sensitive parts if not fully externalized

    @Column(nullable = true) // Nullable if sourceType is API_PROVIDED
    private String username; // Consider encryption

    @Lob
    @Column(nullable = true) // Nullable if sourceType is API_PROVIDED
    private String password; // Consider encryption

    @Lob
    @Column(nullable = true) // Only for API_PROVIDED type
    private String credentialsKey; // Consider encryption

    @Lob
    @Column(columnDefinition = "TEXT") // For storing JSON string for HikariCP params
    private String connectionPoolConfig;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Future considerations: createdBy, updatedBy fields
}

