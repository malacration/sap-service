package br.andrew.sap.services.comercial

import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.bank.WizardPaymentMethodService
import br.andrew.sap.services.documents.OrdersService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Dados da negociacao que vao na minuta do contrato de venda futura: forma de pagamento e
 * parcelamento. O contrato nao guarda a forma; ela e a condicao vem do pedido que o originou
 * (U_orderDocEntry) - a mesma fonte que createAdiantamentoBycontrato usa para emitir os boletos, entao
 * a quantidade de parcelas da minuta e a quantidade de boletos que serao emitidos.
 */
@Service
class NegociacaoContratoService(
    private val contratoService: ContratoVendaFuturaService,
    private val orderService: OrdersService,
    private val paymentTermsTypesService: PaymentTermsTypesService,
    private val wizardPaymentMethodService: WizardPaymentMethodService,
    private val condicaoPagamentoContratoService: CondicaoPagamentoContratoService
) {

    private val logger = LoggerFactory.getLogger(NegociacaoContratoService::class.java)

    fun negociacao(docEntry: Int): NegociacaoContrato {
        val contrato = contratoService.getById(docEntry).tryGetValue<Contrato>()
        val pedido = orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        //so a condicao do pedido, sem cair na do contrato: e dela que a emissao dos boletos tira as
        //parcelas, e sem ela a emissao falha - a minuta nao pode prometer parcelas que nao vao sair
        val condicao = pedido.paymentGroupCode
        val forma = pedido.paymentMethod?.takeIf { it.isNotBlank() }
        return NegociacaoContrato(
            formaPagamentoCodigo = forma,
            formaPagamento = forma?.let { descricaoForma(it) ?: it },
            condicaoPagamentoCodigo = condicao,
            condicaoPagamento = condicao?.let { condicaoPagamentoContratoService.nome(it) },
            quantidadeParcelas = condicao?.let { quantidadeParcelas(it) }
        )
    }

    /** A vista (-1) nao tem linhas em CTG1; condicao sem linha tambem e pagamento unico. */
    fun quantidadeParcelas(condicao: Int): Int {
        if(condicao == CondicaoPagamentoContratoService.A_VISTA)
            return 1
        return paymentTermsTypesService.getParcelas(condicao).size.coerceAtLeast(1)
    }

    private fun descricaoForma(codigo: String): String? {
        return try {
            wizardPaymentMethodService.getById("'${codigo.replace("'", "''")}'").tryGetValue<PaymentMethod>().Description
        } catch (e: Exception) {
            logger.warn("Nao foi possivel carregar a descricao da forma de pagamento [$codigo]", e)
            null
        }
    }
}

data class NegociacaoContrato(
    val formaPagamentoCodigo: String?,
    val formaPagamento: String?,
    val condicaoPagamentoCodigo: Int?,
    val condicaoPagamento: String?,
    val quantidadeParcelas: Int?
)
