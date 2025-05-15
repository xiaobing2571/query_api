package com.example.queryapi.dao.jpa.repository;

import com.example.queryapi.dao.jpa.entity.SqlTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SqlTemplateRepository extends JpaRepository<SqlTemplateEntity, Long> {

    Optional<SqlTemplateEntity> findBySqlCode(String sqlCode);

    boolean existsBySqlCode(String sqlCode);

    void deleteBySqlCode(String sqlCode);
}

