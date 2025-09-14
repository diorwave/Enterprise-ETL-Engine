package com.tia.etl.parser.api.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ValidationResultTest {
    
    @Test
    fun `should create successful validation result`() {
        val result = ValidationResult.success(
            fileSize = 1024,
            estimatedRecords = 100,
            validationTime = 500,
            warnings = listOf("Minor format issue"),
            metadata = mapOf("encoding" to "UTF-8")
        )
        
        assertThat(result.isValid).isTrue()
        assertThat(result.errors).isEmpty()
        assertThat(result.warnings).hasSize(1)
        assertThat(result.fileSize).isEqualTo(1024)
        assertThat(result.estimatedRecords).isEqualTo(100)
        assertThat(result.validationTime).isEqualTo(500)
    }
    
    @Test
    fun `should create failed validation result`() {
        val errors = listOf("Invalid format", "Missing header")
        val result = ValidationResult.failure(
            errors = errors,
            fileSize = 512,
            validationTime = 200
        )
        
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsExactlyElementsOf(errors)
        assertThat(result.estimatedRecords).isNull()
        assertThat(result.fileSize).isEqualTo(512)
    }
    
    @Test
    fun `should create failed result with single error`() {
        val result = ValidationResult.failure(
            error = "File is corrupted",
            fileSize = 256
        )
        
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).containsExactly("File is corrupted")
        assertThat(result.fileSize).isEqualTo(256)
    }
    
    @Test
    fun `should require errors for invalid results`() {
        assertThatThrownBy {
            ValidationResult(
                isValid = false,
                errors = emptyList(), // Invalid: no errors for failed validation
                fileSize = 100
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Invalid files must have at least one error")
    }
    
    @Test
    fun `should require at least one error for failure factory methods`() {
        assertThatThrownBy {
            ValidationResult.failure(emptyList())
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Failure must have at least one error")
    }
    
    @Test
    fun `should calculate total issues correctly`() {
        val result = ValidationResult.success(
            fileSize = 1024,
            warnings = listOf("Warning 1", "Warning 2"),
            metadata = emptyMap()
        )
        
        assertThat(result.getTotalIssues()).isEqualTo(2)
        assertThat(result.hasIssues()).isTrue()
        
        val cleanResult = ValidationResult.success(fileSize = 1024)
        assertThat(cleanResult.getTotalIssues()).isEqualTo(0)
        assertThat(cleanResult.hasIssues()).isFalse()
    }
    
    @Test
    fun `should generate appropriate summaries`() {
        val validResult = ValidationResult.success(
            fileSize = 1024,
            estimatedRecords = 100,
            warnings = listOf("Minor issue")
        )
        
        val invalidResult = ValidationResult.failure(
            errors = listOf("Major error", "Another error"),
            warnings = listOf("Also this warning")
        )
        
        val validSummary = validResult.getSummary()
        assertThat(validSummary).contains("VALID")
        assertThat(validSummary).contains("1 warnings")
        assertThat(validSummary).contains("1024 bytes")
        assertThat(validSummary).contains("~100 records")
        
        val invalidSummary = invalidResult.getSummary()
        assertThat(invalidSummary).contains("INVALID")
        assertThat(invalidSummary).contains("2 errors")
        assertThat(invalidSummary).contains("1 warnings")
    }
    
    @Test
    fun `should validate non-negative values`() {
        assertThatThrownBy {
            ValidationResult(
                isValid = true,
                fileSize = -1
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("File size cannot be negative")
        
        assertThatThrownBy {
            ValidationResult(
                isValid = true,
                estimatedRecords = -5,
                fileSize = 100
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Estimated records cannot be negative")
        
        assertThatThrownBy {
            ValidationResult(
                isValid = true,
                validationTime = -100,
                fileSize = 100
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Validation time cannot be negative")
    }
}