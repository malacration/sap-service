package br.andrew.sap.services.logistica

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Utilizacoes (OUSG.ID) da linha do pedido que entram no carregamento: busca de pedidos da ordem e
 * painel integrado de vendas leem daqui para nao divergirem. Variavel CARREGAMENTO_UTILIZACOES.
 */
@Component
class CarregamentoUtilizacoes(
    @Value("\${carregamento.utilizacoes:\${CARREGAMENTO_UTILIZACOES:9}}")
    configuradas: List<Int?>,
) {
    val lista: List<Int> = utilizacoesOuPadrao(configuradas)

    companion object {
        /** Valor vazio ("") chega como lista vazia e item vazio ("9,,10") como null: sem nenhum valido vale 9. */
        internal fun utilizacoesOuPadrao(configuradas: List<Int?>): List<Int> = configuradas.filterNotNull().ifEmpty { listOf(9) }
    }
}
