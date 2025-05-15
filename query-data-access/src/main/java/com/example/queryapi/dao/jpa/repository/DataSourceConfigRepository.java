package com.example.queryapi.dao.jpa.repository;

import com.example.queryapi.dao.jpa.entity.DataSourceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfigEntity, Long> {

    Optional<DataSourceConfigEntity> findByDatasourceId(String datasourceId);

    boolean existsByDatasourceId(String datasourceId);

    void deleteByDatasourceId(String datasourceId);
}

