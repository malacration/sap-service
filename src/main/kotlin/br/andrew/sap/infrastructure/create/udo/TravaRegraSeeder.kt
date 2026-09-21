package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.services.security.TravaRegraService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Semeia as regras de trava iniciais. Recebe TravaRegraConfiguration no
 * construtor só para garantir que a tabela/UDO já exista antes de semear
 * (mesmo truque de ordenação do CobrancaDominioSeeder).
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.trava"], havingValue = "true", matchIfMissing = false)
class TravaRegraSeeder(
    travaRegraConfiguration: TravaRegraConfiguration,
    val service: TravaRegraService
) {

    init {
        val regras = listOf(
            "DESCONTO" to "Desconto acima do limite",
            "CUSTO" to "Desvio de custo na entrada",
            "DEPOSITO" to "Depósito divergente (500.05)",
        )
        regras.forEachIndexed { index, (codigo, descricao) ->
            service.findOrCreate(codigo, descricao, index + 1)
        }
    }
}
