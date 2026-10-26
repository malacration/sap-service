package br.andrew.sap.infrastructure.configurations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Liga todos os @Scheduled. `jobs.scheduling.enabled=false` desliga TODOS de uma vez - para subir
 * uma instancia local apontada para producao sem rodar as rotinas que ja rodam la (venda futura,
 * desonerado, centro de custo...): varias nao tem chave propria e algumas ligam sozinhas.
 * Sem a chave, fica ligado como sempre foi.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(value = ["jobs.scheduling.enabled"], havingValue = "true", matchIfMissing = true)
class SchedulingConfig
