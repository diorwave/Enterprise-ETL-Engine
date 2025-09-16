package com.quantumsoft.tia.scanner.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Value("\${server.port}")
    private lateinit var serverPort: String
    
    @Value("\${server.servlet.context-path}")
    private lateinit var contextPath: String
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("File Scanner Service API")
                    .version("1.0.0")
                    .description("""
                        File Scanner Service for TIA ETL System.
                        
                        This service is responsible for:
                        - Scanning directories for new files
                        - Validating file formats
                        - Queueing files for processing
                        - Managing scan jobs and schedules
                        - Providing metrics and status information
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("TIA ETL Team")
                            .email("tia-etl@quantumsoft.com")
                    )
                    .license(
                        License()
                            .name("Proprietary")
                            .url("https://quantumsoft.com/license")
                    )
            )
            .addServersItem(
                Server()
                    .url("http://localhost:$serverPort$contextPath")
                    .description("Local development server")
            )
            .addServersItem(
                Server()
                    .url("https://api.tia-etl.gov.lr$contextPath")
                    .description("Production server")
            )
    }
}