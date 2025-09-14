package com.tia.etl.parser.api.models

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration

class ProcessingResultTest {
    
    @Test
    fun `should create valid processing result`() {
        val result = ProcessingResult(
            totalRecords = 100,
            successfulRecords = 95,
            failedRecords = 5,
            processingTime = Duration.ofSeconds(30)
        )
        
        assertThat(result.totalRecords).isEqualTo(100)
        assertThat(result.successfulRecords).isEqualTo(95)
        assertThat(result.failedRecords).isEqualTo(5)
        assertThat(result.skippedRecords).isEqualTo(0) // default value
    }
    
    @Test
    fun `should calculate success rate correctly`() {
        val result = ProcessingResult(
            totalRecords = 100,
            successfulRecords = 80,
            failedRecords = 20,
            processingTime = Duration.ofSeconds(10)
        )
        
        assertThat(result.getSuccessRate()).isEqualTo(0.8)
        assertThat(result.getErrorRate()).isEqualTo(0.2)
    }
    
    @Test
    fun `should handle zero records correctly`() {
        val result = ProcessingResult(
            totalRecords = 0,
            successfulRecords = 0,
            failedRecords = 0,
            processingTime = Duration.ofSeconds(1)
        )
        
        assertThat(result.getSuccessRate()).isEqualTo(1.0)
        assertThat(result.getErrorRate()).isEqualTo(0.0)
        assertThat(result.getRecordsPerSecond()).isEqualTo(0.0)
    }
    
    @Test
    fun `should calculate processing rates`() {
        val result = ProcessingResult(
            totalRecords = 1000,
            successfulRecords = 1000,
            failedRecords = 0,
            processingTime = Duration.ofSeconds(10),
            bytesProcessed = 1024 * 1024 * 10 // 10 MB
        )
        
        assertThat(result.getRecordsPerSecond()).isEqualTo(100.0)
        assertThat(result.getMegabytesPerSecond()).isEqualTo(1.0)
    }
    
    @Test
    fun `should validate record counts consistency`() {
        assertThatThrownBy {
            ProcessingResult(
                totalRecords = 100,
                successfulRecords = 80,
                failedRecords = 30, // 80 + 30 = 110 > 100
                processingTime = Duration.ofSeconds(10)
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining("Sum of successful, failed, and skipped records must equal total")
    }
    
    @Test
    fun `should identify complete success`() {
        val successResult = ProcessingResult(
            totalRecords = 100,
            successfulRecords = 100,
            failedRecords = 0,
            processingTime = Duration.ofSeconds(10)
        )
        
        val failureResult = ProcessingResult(
            totalRecords = 100,
            successfulRecords = 95,
            failedRecords = 5,
            processingTime = Duration.ofSeconds(10)
        )
        
        assertThat(successResult.isCompletelySuccessful()).isTrue()
        assertThat(successResult.hasFailures()).isFalse()
        
        assertThat(failureResult.isCompletelySuccessful()).isFalse()
        assertThat(failureResult.hasFailures()).isTrue()
    }
    
    @Test
    fun `should handle skipped records`() {
        val result = ProcessingResult(
            totalRecords = 100,
            successfulRecords = 80,
            failedRecords = 10,
            skippedRecords = 10,
            processingTime = Duration.ofSeconds(10)
        )
        
        assertThat(result.totalRecords).isEqualTo(100)
        assertThat(result.skippedRecords).isEqualTo(10)
        assertThat(result.getSuccessRate()).isEqualTo(0.8) // success rate based on successful/total
    }
    
    @Test
    fun `should create meaningful summary`() {
        val result = ProcessingResult(
            totalRecords = 1000,
            successfulRecords = 950,
            failedRecords = 50,
            processingTime = Duration.ofMillis(5000),
            errors = listOf(
                ProcessingError("Test error 1"),
                ProcessingError("Test error 2")
            )
        )
        
        val summary = result.getSummary()
        assertThat(summary).contains("total=1000")
        assertThat(summary).contains("successful=950")
        assertThat(summary).contains("failed=50")
        assertThat(summary).contains("time=5000ms")
        assertThat(summary).contains("errors=2")
    }
}