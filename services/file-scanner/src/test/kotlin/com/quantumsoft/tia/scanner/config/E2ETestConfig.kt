package com.quantumsoft.tia.scanner.config

import com.quantumsoft.tia.scanner.scheduler.JobScheduler
import org.mockito.Mockito
import org.quartz.Scheduler
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@TestConfiguration
@Profile("e2e")
class E2ETestConfig {
    
    @Bean
    @Primary
    fun mockScheduler(): Scheduler = Mockito.mock(Scheduler::class.java)
    
    @Bean
    @Primary
    fun mockJobScheduler(): JobScheduler = Mockito.mock(JobScheduler::class.java)
    
    @Bean
    @Primary
    fun mockSchedulerFactoryBean(): org.springframework.scheduling.quartz.SchedulerFactoryBean {
        val factoryBean = org.springframework.scheduling.quartz.SchedulerFactoryBean()
        factoryBean.setAutoStartup(false)
        return factoryBean
    }
}