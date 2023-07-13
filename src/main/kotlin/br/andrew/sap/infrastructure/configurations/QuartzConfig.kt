package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.schedules.AutoApprovalPaymentCondition
import br.andrew.sap.schedules.GeneratePix
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class QuartzConfig {

    @Bean
    fun sempleJob(): JobDetail {
        return JobBuilder.newJob().ofType(AutoApprovalPaymentCondition::class.java)
                .storeDurably()
                .withIdentity("Qrtz_Job_Detail")
                .withDescription("Invoke Sample Job service...")
                .build()
    }

//    @Bean
//    fun generatePix(): JobDetail {
//        return JobBuilder.newJob().ofType(GeneratePix::class.java)
//            .storeDurably()
//            .withIdentity("qrtz_job_pix")
//            .withDescription("Iniciando job pix")
//            .build()
//    }

    @Bean
    fun triggerForever(job: JobDetail): Trigger {
        return TriggerBuilder
            .newTrigger()
            .forJob(job.also { it.isConcurrentExectionDisallowed  })
            .withIdentity("Qrtz_Trigger")
            .withDescription("Sample trigger")
            .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(30))
                .build()
    }
}