package com.tia.etl.parser.api.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ParserMetadataTest {
    
    private val validSchema = TableSchema(
        tableName = "test_table",
        columns = listOf(
            ColumnDefinition("id", "UInt32"),
            ColumnDefinition("name", "String")
        )
    )
    
    @Test
    fun `should create valid parser metadata`() {
        val metadata = ParserMetadata(
            parserId = "test-parser",
            name = "Test Parser",
            version = "1.0.0",
            description = "A test parser for unit tests",
            supportedFormats = listOf("csv", "txt"),
            targetTable = "test_table",
            schemaDefinition = validSchema
        )
        
        assertThat(metadata.parserId).isEqualTo("test-parser")
        assertThat(metadata.name).isEqualTo("Test Parser")
        assertThat(metadata.version).isEqualTo("1.0.0")
        assertThat(metadata.supportedFormats).containsExactly("csv", "txt")
        assertThat(metadata.requiresValidation).isTrue() // default value
    }
    
    @Test
    fun `should support format check case insensitive`() {
        val metadata = ParserMetadata(
            parserId = "test-parser",
            name = "Test Parser",
            version = "1.0.0",
            description = "A test parser",
            supportedFormats = listOf("CSV", "txt"),
            targetTable = "test_table",
            schemaDefinition = validSchema
        )
        
        assertThat(metadata.supportsFormat("csv")).isTrue()
        assertThat(metadata.supportsFormat("CSV")).isTrue()
        assertThat(metadata.supportsFormat("TXT")).isTrue()
        assertThat(metadata.supportsFormat("json")).isFalse()
    }
    
    @Test
    fun `should reject invalid parser ID format`() {
        assertThatThrownBy {
            ParserMetadata(
                parserId = "Invalid Parser ID",
                name = "Test Parser",
                version = "1.0.0",
                description = "A test parser",
                supportedFormats = listOf("csv"),
                targetTable = "test_table",
                schemaDefinition = validSchema
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Parser ID must be lowercase")
    }
    
    @Test
    fun `should reject invalid version format`() {
        assertThatThrownBy {
            ParserMetadata(
                parserId = "test-parser",
                name = "Test Parser",
                version = "invalid-version",
                description = "A test parser",
                supportedFormats = listOf("csv"),
                targetTable = "test_table",
                schemaDefinition = validSchema
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Version must follow semantic versioning")
    }
    
    @Test
    fun `should accept pre-release version format`() {
        val metadata = ParserMetadata(
            parserId = "test-parser",
            name = "Test Parser",
            version = "1.0.0-alpha",
            description = "A test parser",
            supportedFormats = listOf("csv"),
            targetTable = "test_table",
            schemaDefinition = validSchema
        )
        
        assertThat(metadata.version).isEqualTo("1.0.0-alpha")
    }
    
    @Test
    fun `should require at least one supported format`() {
        assertThatThrownBy {
            ParserMetadata(
                parserId = "test-parser",
                name = "Test Parser",
                version = "1.0.0",
                description = "A test parser",
                supportedFormats = emptyList(),
                targetTable = "test_table",
                schemaDefinition = validSchema
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("must support at least one format")
    }
    
    @Test
    fun `should validate batch size and max file size`() {
        assertThatThrownBy {
            ParserMetadata(
                parserId = "test-parser",
                name = "Test Parser",
                version = "1.0.0",
                description = "A test parser",
                supportedFormats = listOf("csv"),
                targetTable = "test_table",
                schemaDefinition = validSchema,
                batchSize = -1
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Batch size must be positive")
        
        assertThatThrownBy {
            ParserMetadata(
                parserId = "test-parser",
                name = "Test Parser",
                version = "1.0.0",
                description = "A test parser",
                supportedFormats = listOf("csv"),
                targetTable = "test_table",
                schemaDefinition = validSchema,
                maxFileSize = -1
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Max file size must be positive")
    }
}