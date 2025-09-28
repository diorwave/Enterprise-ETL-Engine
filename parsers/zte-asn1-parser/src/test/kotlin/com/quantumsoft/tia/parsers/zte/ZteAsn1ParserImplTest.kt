package com.quantumsoft.tia.parsers.zte

import com.tia.etl.parser.api.models.ProcessingContext
import com.tia.etl.parser.api.models.JobConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.Connection
import java.util.UUID
import org.slf4j.Logger
import com.quantumsoft.tia.parsers.zte.asn1.Asn1DecoderImpl
import com.quantumsoft.tia.parsers.zte.database.ClickHouseWriter
import com.quantumsoft.tia.parsers.zte.validation.ZteDataValidator
import com.quantumsoft.tia.parsers.zte.utils.MetricsCollectorImpl

class ZteAsn1ParserImplTest {
    
    private lateinit var parser: ZteAsn1ParserImpl
    private lateinit var decoder: Asn1DecoderImpl
    private lateinit var validator: ZteDataValidator
    private lateinit var writer: ClickHouseWriter
    private lateinit var metrics: MetricsCollectorImpl
    
    @TempDir
    lateinit var tempDir: File
    
    @BeforeEach
    fun setUp() {
        decoder = mockk(relaxed = true)
        validator = mockk(relaxed = true)
        writer = mockk(relaxed = true)
        metrics = mockk(relaxed = true)
        
        parser = ZteAsn1ParserImpl(decoder, validator, writer, metrics)
    }
    
    @Test
    fun `should return correct metadata`() {
        // When
        val metadata = parser.getMetadata()
        
        // Then
        assertThat(metadata.parserId).isEqualTo("zte-asn1-parser")
        assertThat(metadata.name).isEqualTo("ZTE ASN.1 CDR Parser")
        assertThat(metadata.version).isEqualTo("1.0.0")
        assertThat(metadata.supportedFormats).containsExactly("asn1", "ber", "cdr")
        assertThat(metadata.targetTable).isEqualTo("zte_cdr_records")
        assertThat(metadata.author).isEqualTo("Quantum Soft TIA Team")
    }
    
    @Test
    fun `should initialize components correctly`() {
        // Given
        val config = mapOf(
            "batchSize" to 1000,
            "blockSize" to 4096
        )
        
        // When
        parser.initialize(config)
        
        // Then
        verify { decoder.initialize(config) }
        verify { validator.initialize(config) }
        verify { writer.initialize(config) }
        verify { metrics.initialize(config) }
    }
    
    @Test
    fun `should validate file existence`() {
        // Given
        val existingFile = File(tempDir, "test.asn1")
        existingFile.writeText("test data")
        
        // When
        val result = parser.validate(existingFile)
        
        // Then
        assertThat(result.isValid).isTrue()
        assertThat(result.errors).isEmpty()
    }
    
    @Test
    fun `should detect non-existent file`() {
        // Given
        val nonExistentFile = File(tempDir, "does_not_exist.asn1")
        
        // When
        val result = parser.validate(nonExistentFile)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).isNotEmpty()
        assertThat(result.errors.first()).contains("FILE_NOT_FOUND")
    }
    
    @Test
    fun `should detect empty file`() {
        // Given
        val emptyFile = File(tempDir, "empty.asn1")
        emptyFile.createNewFile()
        
        // When
        val result = parser.validate(emptyFile)
        
        // Then
        assertThat(result.isValid).isFalse()
        assertThat(result.errors).isNotEmpty()
        assertThat(result.errors.first()).contains("FILE_EMPTY")
    }
    
    @Test
    fun `should shutdown components correctly`() {
        // When
        parser.shutdown()
        
        // Then
        verify { decoder.destroy() }
        verify { validator.destroy() }
        verify { writer.destroy() }
        verify { metrics.destroy() }
    }
    
    @Test
    fun `should process file successfully`() {
        // Given
        val testFile = File(tempDir, "test.asn1")
        testFile.writeBytes(byteArrayOf(0x30, 0x10)) // Simple ASN.1 SEQUENCE
        
        val jobConfig = mockk<JobConfiguration>(relaxed = true)
        
        val context = ProcessingContext(
            file = testFile,
            processingId = UUID.randomUUID(),
            jobConfig = jobConfig,
            clickHouseConnection = mockk(relaxed = true),
            postgresConnection = mockk(relaxed = true),
            logger = mockk(relaxed = true)
        )
        
        // Mock decoder behavior
        every { decoder.decode(any(), any(), any()) } returns 
            DecodeResult.Failure("End of data")
        
        // When
        val result = parser.process(context)
        
        // Then
        assertThat(result.totalRecords).isEqualTo(0)
        assertThat(result.successfulRecords).isEqualTo(0)
        assertThat(result.failedRecords).isEqualTo(0)
        assertThat(result.bytesProcessed).isEqualTo(2L)
    }
}
