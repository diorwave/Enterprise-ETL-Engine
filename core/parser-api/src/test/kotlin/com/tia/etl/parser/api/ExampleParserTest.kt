package com.tia.etl.parser.api

import com.tia.etl.parser.api.exceptions.ParserException
import com.tia.etl.parser.api.exceptions.ValidationException
import com.tia.etl.parser.api.models.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import java.io.File
import java.sql.Connection
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * Example parser implementation for testing the API contracts.
 * 
 * This demonstrates how to implement both DataParser and ParserLifecycle
 * interfaces correctly.
 */
class ExampleCsvParser : DataParser, ParserLifecycle {
    
    private var initialized = false
    private var batchSize = 1000
    
    override fun initialize(config: Map<String, Any>) {
        batchSize = (config["batchSize"] as? Number)?.toInt() ?: 1000
        if (batchSize <= 0) {
            throw ParserException("Batch size must be positive: $batchSize")
        }
        initialized = true
    }
    
    override fun validate(file: File): ValidationResult {
        if (!file.exists()) {
            throw ValidationException("File not found: ${file.absolutePath}")
        }
        
        if (!file.canRead()) {
            return ValidationResult.failure("File is not readable")
        }
        
        if (file.length() == 0L) {
            return ValidationResult.failure("File is empty")
        }
        
        // Simple validation - check if it's a CSV by looking for commas in first line
        val firstLine = file.readLines().firstOrNull()
        if (firstLine == null || !firstLine.contains(",")) {
            return ValidationResult.failure("File does not appear to be CSV format")
        }
        
        val estimatedRecords = file.readLines().size.toLong() - 1 // Subtract header
        return ValidationResult.success(
            fileSize = file.length(),
            estimatedRecords = maxOf(0, estimatedRecords),
            validationTime = 50
        )
    }
    
    override fun getMetadata(): ParserMetadata {
        return ParserMetadata(
            parserId = "example-csv-parser",
            name = "Example CSV Parser",
            version = "1.0.0",
            description = "Example parser for CSV files",
            supportedFormats = listOf("csv", "txt"),
            targetTable = "example_data",
            schemaDefinition = TableSchema(
                tableName = "example_data",
                columns = listOf(
                    ColumnDefinition("id", "UInt32"),
                    ColumnDefinition("name", "String"),
                    ColumnDefinition("timestamp", "DateTime")
                ),
                primaryKey = listOf("id"),
                orderBy = listOf("timestamp")
            ),
            batchSize = batchSize
        )
    }
    
    override fun process(context: ProcessingContext): ProcessingResult {
        if (!initialized) {
            throw ParserException("Parser not initialized", "example-csv-parser")
        }
        
        context.log(LogLevel.INFO, "Starting CSV processing")
        
        val startTime = System.currentTimeMillis()
        val lines = context.file.readLines()
        
        // Skip header
        val dataLines = lines.drop(1)
        var successful = 0L
        var failed = 0L
        val errors = mutableListOf<ProcessingError>()
        
        // Simulate processing records in batches
        dataLines.forEachIndexed { index, line ->
            try {
                val parts = line.split(",")
                if (parts.size >= 3) {
                    // Simulate database insert (would use real ClickHouse connection)
                    successful++
                } else {
                    failed++
                    errors.add(
                        ProcessingError(
                            message = "Invalid CSV format: expected at least 3 columns",
                            recordNumber = index + 2L, // +2 because we skipped header and arrays are 0-indexed
                            lineNumber = index + 2L
                        )
                    )
                }
            } catch (e: Exception) {
                failed++
                errors.add(
                    ProcessingError(
                        message = "Error processing record: ${e.message}",
                        recordNumber = index + 2L,
                        exception = e
                    )
                )
            }
        }
        
        val processingTime = Duration.ofMillis(System.currentTimeMillis() - startTime)
        context.log(LogLevel.INFO, "Completed processing: $successful successful, $failed failed")
        
        return ProcessingResult(
            totalRecords = dataLines.size.toLong(),
            successfulRecords = successful,
            failedRecords = failed,
            processingTime = processingTime,
            errors = errors,
            bytesProcessed = context.getFileSize()
        )
    }
    
    override fun shutdown() {
        initialized = false
        // In a real implementation, close resources here
    }
}

class ExampleParserTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var parser: ExampleCsvParser
    private lateinit var mockClickHouseConnection: Connection
    private lateinit var mockPostgresConnection: Connection
    private lateinit var mockLogger: Logger
    
    @BeforeEach
    fun setUp() {
        parser = ExampleCsvParser()
        mockClickHouseConnection = mockk<Connection>()
        mockPostgresConnection = mockk<Connection>()
        mockLogger = mockk<Logger>(relaxed = true)
        
        every { mockClickHouseConnection.isClosed } returns false
        every { mockPostgresConnection.isClosed } returns false
    }
    
    @Test
    fun `should initialize parser successfully`() {
        val config = mapOf("batchSize" to 500)
        
        parser.initialize(config)
        
        val metadata = parser.getMetadata()
        assertThat(metadata.parserId).isEqualTo("example-csv-parser")
        assertThat(metadata.batchSize).isEqualTo(500)
    }
    
    @Test
    fun `should validate CSV file successfully`() {
        val csvFile = File(tempDir, "test.csv").apply {
            writeText("id,name,timestamp\n1,John,2023-01-01\n2,Jane,2023-01-02")
        }
        
        parser.initialize(emptyMap())
        val result = parser.validate(csvFile)
        
        assertThat(result.isValid).isTrue()
        assertThat(result.estimatedRecords).isEqualTo(2)
        assertThat(result.fileSize).isEqualTo(csvFile.length())
    }
    
    @Test
    fun `should reject non-CSV file`() {
        val txtFile = File(tempDir, "test.txt").apply {
            writeText("This is not a CSV file")
        }
        
        parser.initialize(emptyMap())
        val result = parser.validate(txtFile)
        
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).isNotEmpty()
        assertThat(result.errors.first()).contains("CSV format")
    }
    
    @Test
    fun `should process CSV file successfully`() {
        val csvFile = File(tempDir, "test.csv").apply {
            writeText("id,name,timestamp\n1,John,2023-01-01\n2,Jane,2023-01-02\n3,Bob")
        }
        
        parser.initialize(emptyMap())
        
        val context = ProcessingContext(
            file = csvFile,
            clickHouseConnection = mockClickHouseConnection,
            postgresConnection = mockPostgresConnection,
            jobConfig = createTestJobConfig(),
            logger = mockLogger
        )
        
        val result = parser.process(context)
        
        assertThat(result.totalRecords).isEqualTo(3)
        assertThat(result.successfulRecords).isEqualTo(2) // First two have 3+ columns
        assertThat(result.failedRecords).isEqualTo(1) // Third one has only 2 columns
        assertThat(result.errors).hasSize(1)
        assertThat(result.isCompletelySuccessful()).isFalse()
        assertThat(result.hasFailures()).isTrue()
        
        // Verify logging was called - simplified version without specific matching
        verify(atLeast = 2) { mockLogger.info(any<String>(), any()) }
    }
    
    @Test
    fun `should shutdown gracefully`() {
        parser.initialize(emptyMap())
        
        // Should not throw exception
        parser.shutdown()
    }
    
    private fun createTestJobConfig(): JobConfiguration {
        return JobConfiguration(
            id = UUID.randomUUID(),
            name = "Test Job",
            sourceDirectory = "/tmp/test",
            filePattern = ".*\\.csv$", // Fixed: proper regex for CSV files
            scanInterval = ScanInterval.Fixed(Duration.ofMinutes(5)),
            parserId = "example-csv-parser",
            afterProcessing = ProcessingAction.ARCHIVE,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            createdBy = "test-user"
        )
    }
}