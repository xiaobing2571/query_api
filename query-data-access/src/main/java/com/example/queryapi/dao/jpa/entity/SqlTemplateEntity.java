package com.example.queryapi.dao.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sql_templates", indexes = {
        @Index(name = "idx_sql_code_unique", columnList = "sqlCode", unique = true)
})
@Data
@EqualsAndHashCode(of = "id")
public class SqlTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sqlCode; // Business Key

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sqlContent;

    @Lob
    private String description;

    @Version // For optimistic locking
    private Integer version;

    @Column(nullable = true) // Optional hint for dialect-specific SQL
    private String dataSourceTypeHint;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private String createdBy;

    private String lastModifiedBy;
}

