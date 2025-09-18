package com.quantumsoft.tia.scanner.config

import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SchedulerFactory
import org.quartz.impl.StdSchedulerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import java.util.Properties

@TestConfiguration
@Profile("test")
class TestQuartzConfig {
    
    @Bean
    @Primary
    fun testSchedulerFactoryBean(
        applicationContext: ApplicationContext,
        jobFactory: SpringBeanJobFactory
    ): SchedulerFactoryBean {
        val factory = SchedulerFactoryBean()
        factory.setJobFactory(jobFactory)
        
        // Use memory job store with minimal configuration
        val props = Properties()
        props["org.quartz.scheduler.instanceName"] = "TestScheduler"
        props["org.quartz.scheduler.instanceId"] = "AUTO"
        props["org.quartz.threadPool.threadCount"] = "1"
        props["org.quartz.jobStore.class"] = "org.quartz.simpl.RAMJobStore"
        
        factory.setQuartzProperties(props)
        factory.setOverwriteExistingJobs(true)
        factory.setAutoStartup(false) // Don't auto-start for tests
        factory.setWaitForJobsToCompleteOnShutdown(true)
        factory.setSchedulerName("TestFileScannerScheduler")
        return factory
    }
}