package br.andrew.sap.schedules.futura

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.payment.HandlePaymentTermsLines
import br.andrew.sap.model.sap.comercial.DocEntry
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.self.vendafutura.ContratoParse
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.comercial.ContratoVendaFuturaService
import br.andrew.sap.services.comercial.FreteContratoService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.fiscal.OrigemBoletoEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = ["venda-futura.utilizacao"], matchIfMissing = false)
class VendaFuturaScheduled(
    protected val authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
    val contratoService : ContratoVendaFuturaService,
    val paymentService : PaymentTermsTypesService,
    val adiantamentoService : DownPaymentService,
    val bankplus : BankPlusService,
    val orderService : OrdersService,
    val freteContratoService : FreteContratoService,
    @Value("\${venda-futura.utilizacao:-1}") val idUtilizacao : Long,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.carencia:7}") val carenciaDias : Int,
    protected val env: SapEnvrioment) {

    val logger: Logger = LoggerFactory.getLogger(VendaFuturaScheduled::class.java)


    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    fun execute() {
        filiais.forEach { filial ->
        sqlQueriesService.execute(
            "vendafutura-aberto.sql",
            listOf(Parameter("utilizacao",idUtilizacao),
            Parameter("filiais",filial),
            Parameter("startDate","2024-12-09"))
        )?.tryGetValues<DocEntry>()?.forEach {
            orderService.get(Filter("DocEntry",it.DocEntry!!,Condicao.EQUAL))
                .tryGetValues<Document>().forEach { order ->
                    val (localidade, regiao) = destinoDoPedido(order)
                    contratoService.saveOnly(ContratoParse.parse(order, localidade, regiao))
                    orderService.close(order.docEntry.toString())
                }
            }}
    }

    /**
     * Localidade de entrega negociada e a regiao que valia na assinatura.
     *
     * Best effort de proposito: falha aqui NAO pode impedir o contrato de nascer. Este schedule
     * roda a cada 30s e so fecha o pedido depois de salvar o contrato - propagar a excecao
     * deixaria o pedido preso num laco de erro por causa de cadastro de frete faltando. Sem
     * destino, o contrato nasce legado: a retirada segue normal e a troca exige atribuir antes.
     */
    private fun destinoDoPedido(order: Document): Pair<Int?, String?> {
        return try {
            val filial = order.getBPL_IDAssignedToInvoice().toIntOrNull()
            val localidade = freteContratoService.localidadeDoEndereco(
                order.CardCode, order.shipToCode, AddresType.bo_ShipTo)
            localidade to freteContratoService.regiaoVigente(filial).Code
        } catch (e: Exception) {
            logger.warn("contrato do pedido ${order.docNum} nasce sem localidade de entrega: ${e.message}")
            null to null
        }
    }
}