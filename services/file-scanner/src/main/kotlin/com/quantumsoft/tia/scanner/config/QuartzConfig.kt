package com.quantumsoft.tia.scanner.config

import org.springframework.boot.autoconfigure.quartz.QuartzProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import javax.sql.DataSource

@Configuration
class QuartzConfig {
    
    @Bean
    fun springBeanJobFactory(applicationContext: ApplicationContext): SpringBeanJobFactory {
        val jobFactory = AutowiringSpringBeanJobFactory()
        jobFactory.setApplicationContext(applicationContext)
        return jobFactory
    }
    
    @Bean
    fun schedulerFactoryBean(
        dataSource: DataSource,
        jobFactory: SpringBeanJobFactory,
        quartzProperties: QuartzProperties
    ): SchedulerFactoryBean {
        val factory = SchedulerFactoryBean()
        factory.setDataSource(dataSource)
        factory.setJobFactory(jobFactory)
        val props = java.util.Properties()
        props.putAll(quartzProperties.properties)
        factory.setQuartzProperties(props)
        factory.setOverwriteExistingJobs(true)
        factory.setAutoStartup(true)
        factory.setWaitForJobsToCompleteOnShutdown(true)
        factory.setSchedulerName("FileScannerScheduler")
        return factory
    }
}

/**
 * Custom job factory that enables Spring dependency injection in Quartz jobs
 */
class AutowiringSpringBeanJobFactory : SpringBeanJobFactory() {
    
    private lateinit var applicationContext: ApplicationContext
    
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
        super.setApplicationContext(applicationContext)
    }
    
    override fun createJobInstance(bundle: org.quartz.spi.TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        applicationContext.autowireCapableBeanFactory.autowireBean(job)
        return job
    }
}