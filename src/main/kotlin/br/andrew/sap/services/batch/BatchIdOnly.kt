package br.andrew.sap.services.batch

/**
 * Operacao que so precisa do id na URL e nao leva corpo (DELETE, Cancel, Close).
 *
 * Existe pra cada dominio nao ter que criar a sua classe de uma linha: CarregamentoController
 * (CloseBatchPayload) e DownPayment (DownPaymentUnsetVendaFutura) ja tinham exatamente essa forma.
 */
data class BatchIdOnly(private val id: String) : BatchId {
    override fun getId(): String = id
}
