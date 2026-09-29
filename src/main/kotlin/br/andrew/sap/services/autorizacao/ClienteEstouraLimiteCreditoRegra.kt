package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Venda a prazo em que o saldo em aberto do cliente (vencido e a vencer, OCRD.Balance) MAIS
 * o proprio pedido passam do limite de credito cadastrado (OCRD.CreditLine).
 *
 * Complementa ClienteEmAtrasoRegra em vez de substituir: aquela olha EXISTENCIA de titulo
 * vencido ha mais de 3 dias (qualquer valor), esta olha VALOR contra o limite. Cliente
 * pontual mas ja no teto do limite so e pego por esta - por isso o saldo inclui o que ainda
 * nao venceu; atraso de valor pequeno, so por aquela.
 */
@Component
class ClienteEstouraLimiteCreditoRegra(private val odbcClient: OdbcClient) : RegraAutorizacao {

    private val log = LoggerFactory.getLogger(ClienteEstouraLimiteCreditoRegra::class.java)

    override val motivo = "CLIENTE_ESTOURA_LIMITE_CREDITO"

    //uma leitura so, ja sem os comentarios do arquivo: o validador do sap-odbc recusa
    //instrucao que contenha comentario
    private val sql by lazy { SqlResource.carregar("odbc/cliente-divida-aberta.sql") }

    override fun avalia(documento: Document): Boolean {
        if (documento.CardCode.isBlank())
            return false

        //a vista nao gera risco de credito - o dinheiro entra junto com a venda. Mesmo
        //escopo de ClienteEmAtrasoRegra (paymentGroupCode == -1)
        if (documento.isAvista())
            return false

        val linha = try {
            odbcClient
                .consultar(sql, mapOf("cardCode" to documento.CardCode), maxRows = 1)
                .rows.firstOrNull()
        } catch (e: Exception) {
            //sap-odbc fora do ar nao pode derrubar todo POST de pedido: a regra se abstem e
            //as outras regras do motor seguem valendo. Fica no log porque abster-se aqui
            //significa deixar passar venda que talvez devesse ser retida.
            log.error("Falha ao consultar limite de credito de ${documento.CardCode}", e)
            return false
        } ?: return false

        //limite zerado e "sem limite definido no cadastro", nao "limite zero": tratar como
        //zero jogaria TODA venda a prazo desse cliente em autorizacao
        val limite = numero(linha["LimiteCredito"])
        if (limite <= 0.0)
            return false

        return (numero(linha["DividaAberta"]) + documento.total()) > limite
    }

    /**
     * NUMERIC/DECIMAL do HANA chega como String, nao como numero: o sap-odbc serializa
     * BigDecimal com `stripTrailingZeros().toPlainString()` (QueryService.toJsonValue).
     * Um `as? Number` direto devolveria null em silencio - e a regra nunca dispararia.
     */
    private fun numero(valor: Any?): Double = when (valor) {
        is Number -> valor.toDouble()
        is String -> valor.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}
