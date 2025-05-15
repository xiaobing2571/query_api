package com.example.queryapi.common.enums;

/**
 * Enum representing the type of a data source, indicating how its connection details are obtained.
 */
public enum DataSourceType {
    /**
     * Connection details are provided by an external API call.
     */
    API_PROVIDED,

    /**
     * Connection details are stored and retrieved from the service's own metadata database.
     */
    METADATA_DB
}

