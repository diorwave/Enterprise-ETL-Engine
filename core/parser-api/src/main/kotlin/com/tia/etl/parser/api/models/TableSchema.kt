package com.tia.etl.parser.api.models

/**
 * Represents the database table schema definition for parsed data.
 * 
 * This class defines the structure of the target table where parsed data will be stored,
 * including column definitions, constraints, and indexing information.
 * 
 * @property tableName The name of the target table in ClickHouse
 * @property columns List of column definitions for the table
 * @property primaryKey List of column names that form the primary key (optional)
 * @property partitionBy Column name used for table partitioning in ClickHouse (optional)
 * @property orderBy List of column names used for table ordering in ClickHouse (optional)
 * @property engine The ClickHouse table engine to use (default: MergeTree)
 * @property ttlExpression TTL expression for data retention (optional)
 */
data class TableSchema(
    val tableName: String,
    val columns: List<ColumnDefinition>,
    val primaryKey: List<String> = emptyList(),
    val partitionBy: String? = null,
    val orderBy: List<String> = emptyList(),
    val engine: String = "MergeTree",
    val ttlExpression: String? = null
) {
    init {
        require(tableName.isNotBlank()) { "Table name cannot be blank" }
        require(columns.isNotEmpty()) { "Table must have at least one column" }
        require(columns.map { it.name }.distinct().size == columns.size) {
            "Column names must be unique"
        }
        if (primaryKey.isNotEmpty()) {
            require(primaryKey.all { pk -> columns.any { col -> col.name == pk } }) {
                "Primary key columns must exist in table columns"
            }
        }
        if (orderBy.isNotEmpty()) {
            require(orderBy.all { ob -> columns.any { col -> col.name == ob } }) {
                "Order by columns must exist in table columns"
            }
        }
        partitionBy?.let { partition ->
            require(columns.any { col -> col.name == partition }) {
                "Partition column must exist in table columns"
            }
        }
    }
}

/**
 * Represents a column definition in a table schema.
 * 
 * @property name The column name
 * @property type The ClickHouse data type (e.g., "String", "UInt32", "DateTime", "Nullable(String)")
 * @property nullable Whether the column can contain null values
 * @property defaultValue Default value for the column (optional)
 * @property comment Optional comment describing the column
 */
data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean = false,
    val defaultValue: String? = null,
    val comment: String? = null
) {
    init {
        require(name.isNotBlank()) { "Column name cannot be blank" }
        require(type.isNotBlank()) { "Column type cannot be blank" }
        require(name.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$"))) {
            "Column name must start with a letter and contain only letters, numbers, and underscores"
        }
    }
}