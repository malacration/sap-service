package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.schedules.AutoApprovalPaymentCondition
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class QuartzConfig {

    @Bean
    fun sempleJob(): JobDetail {
        return JobBuilder.newJob().ofType(AutoApprovalPaymentCondition::class.java)
                .storeDurably()
                .withIdentity("Qrtz_Job_Detail")
                .withDescription("Invoke Sample Job service...")
                .build()
    }

    @Bean
    fun triggerForever(job: JobDetail): Trigger {
        return TriggerBuilder.newTrigger().forJob(job.also { it.isConcurrentExectionDisallowed  })
                .withIdentity("Qrtz_Trigger")
                .withDescription("Sample trigger")
                .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(30))
                .build()
    }
}