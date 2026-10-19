package br.andrew.sap.infrastructure

import br.andrew.sap.infrastructure.configurations.SchedulingConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.config.ScheduledTaskHolder
import java.util.concurrent.atomic.AtomicInteger

/** A chave global tem de desligar todo @Scheduled, sem depender da chave de cada rotina. */
class SchedulingConfigTest {

    class Rotina {
        val execucoes = AtomicInteger()
        @Scheduled(fixedDelay = 10)
        fun roda() { execucoes.incrementAndGet() }
    }

    @Configuration
    class ComRotina {
        @Bean
        fun rotina() = Rotina()
    }

    private val contexto = ApplicationContextRunner().withUserConfiguration(SchedulingConfig::class.java, ComRotina::class.java)

    private fun tarefas(ctx: org.springframework.context.ApplicationContext) =
        ctx.getBeansOfType(ScheduledTaskHolder::class.java).values.sumOf { it.scheduledTasks.size }

    @Test
    fun `sem a chave os schedules ficam ligados, como sempre`() {
        contexto.run { ctx ->
            assertEquals(1, tarefas(ctx))
            Thread.sleep(100)
            assertTrue(ctx.getBean(Rotina::class.java).execucoes.get() > 0)
        }
    }

    @Test
    fun `jobs scheduling enabled false desliga todos`() {
        contexto.withPropertyValues("jobs.scheduling.enabled=false").run { ctx ->
            assertEquals(0, tarefas(ctx))
            Thread.sleep(100)
            assertEquals(0, ctx.getBean(Rotina::class.java).execucoes.get())
        }
    }
}
