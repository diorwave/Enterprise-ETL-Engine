package com.quantumsoft.tia.scanner.config

import com.quantumsoft.tia.scanner.scheduler.JobScheduler
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StartupInitializer {
    
    companion object {
        private val logger = LoggerFactory.getLogger(StartupInitializer::class.java)
    }
    
    @Bean
    fun initializeScheduler(jobScheduler: JobScheduler) = CommandLineRunner {
        logger.info("Initializing File Scanner Service...")
        
        try {
            // Reschedule all active jobs on startup
            jobScheduler.rescheduleAllActiveJobs()
            logger.info("File Scanner Service initialization completed successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize File Scanner Service", e)
            // Don't throw - allow service to start even if scheduling fails
        }
    }
}