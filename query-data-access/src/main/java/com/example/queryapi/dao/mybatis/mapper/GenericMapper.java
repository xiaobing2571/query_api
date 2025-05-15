package com.example.queryapi.dao.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface GenericMapper {
    /**
     * Executes a generic select query.
     * The SQL query string is passed as a parameter and should be carefully constructed
     * to prevent SQL injection if dynamic parts are not handled by MyBatis parameter substitution.
     * It's highly recommended that the actual SQL comes from trusted, pre-defined templates.
     *
     * @param sql The SQL query string to execute.
     * @param params A map of parameters to be used in the SQL query.
     * @return A list of maps, where each map represents a row in the result set.
     */
    List<Map<String, Object>> executeSelect(@Param("sql") String sql, @Param("params") Map<String, Object> params);

    // Potentially add executeUpdate, executeInsert, executeDelete if needed in the future
    // int executeUpdate(@Param("sql") String sql, @Param("params") Map<String, Object> params);
}

