package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.HandlePaymentTermsLines
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.BoletoVf
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.schedules.futura.Soma
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.invent.OrigemBoletoEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class DownPaymentService(env: SapEnvrioment,
                         val sqlQueriesService: SqlQueriesService,
                         private val orderService: OrdersService,
                         private val paymentService : PaymentTermsTypesService,
                         private val bankplus : BankPlusService,
                         @Value("\${venda-futura.adiantamento-item:none}") val vfItemAdiantamento : String,
                         @Value("\${adiantamento-vf.formaPagamento:none}") vfFormaPagamento : String,
                         restTemplate: RestTemplate,
                         authService: AuthService,
) : EntitiesService<Document>(env, restTemplate, authService) {

    val vfFormaPagamento: String? = if(vfFormaPagamento == "none")  null else vfFormaPagamento

    val logger: Logger = LoggerFactory.getLogger(DownPaymentService::class.java)

    override fun path(): String {
        return "/b1s/v1/DownPayments"
    }

    fun adiantamentosVendaFuturaSave(contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        val adiantamento = adiantamentosVendaFuturaWithoutSave(contrato,paymentInfo)
        adiantamento.journalMemo = "Fatura de adiantamento venda futura. Contrato ${contrato.DocEntry}"
        adiantamento.comments = adiantamento.journalMemo
        return save(adiantamento).tryGetValue<Document>()
    }

    fun adiantamentosVendaFuturaWithoutSave(contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        if(vfItemAdiantamento == "none")
            throw Exception("O parametro [venda-futura.adiantamento-item] nao pode ser $vfItemAdiantamento")
        val linhas = listOf(Product(vfItemAdiantamento,paymentInfo.value.toString(),"1"))
        val adiantamento = DownPayment(
            contrato.U_cardCode,
            paymentInfo.dueDate.toString(),
            linhas,
            contrato.U_filial.toString())
        if(vfFormaPagamento != null)
            adiantamento.paymentMethod = vfFormaPagamento
        adiantamento.U_venda_futura = contrato.DocEntry;
        return adiantamento
    }

    fun getLastInstallment(contrato: Contrato): String {
        val boleto = getByContratoVendaFutura(
            contrato.DocEntry?: throw Exception("O id do contrato nao pode ser nullo"),
            OrderBy("DocDueDate",Order.DESC)
        ).firstOrNull() ?: throw Exception("Contrato invalido pois nao tem nenhum boleto")
        return boleto.DocDueDate ?: throw Exception("O ultimo boleto nao tem data de vencimento")
    }

    fun getByContratoVendaFutura(id: Int, orderBy : OrderBy = OrderBy()): List<Document> {
        val filter = Filter(
            Predicate("U_venda_futura",id,Condicao.EQUAL)
        )
        return get(filter,orderBy).tryGetValues()
    }

    fun getByContratoVendaFuturaStatus(id: Int): List<BoletoVf> {
        val parametros = listOf(Parameter("idVendaFutura",id),)
        return sqlQueriesService.execute("boletos-status.sql",parametros)?.tryGetValues<BoletoVf>() ?: listOf()
    }

    fun adiantamentosAbertos(invoice : Invoice): List<DownPayment> {
        val exp = Exception("Nao foi possivel buscar o adianemtno, entrega de venda sem id contrato")
        return adiantamentosAbertos(invoice.CardCode,invoice.U_venda_futura?: throw exp)
    }

    fun adiantamentosAbertos(cardCode : String, vendaFutura : Int): List<DownPayment> {
        val adiantamentoFilter = Filter(
            Predicate("U_venda_futura",vendaFutura,Condicao.EQUAL),
            Predicate("CardCode",cardCode,Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Close,Condicao.EQUAL),
            //Talvez DownPaymentStatus indique se tem saldo a apropriar ou nao
        )
        return get(adiantamentoFilter).tryGetValues<DownPayment>()
            .filter {
                (sqlQueriesService
                    .execute("devolucao-adiantamento.sql", Parameter("docEntry", it.docEntry!!))
                    ?.tryGetValues<DocEntry>() ?: listOf()).isEmpty()
            }
            .map{
                val apropriacoes : List<Soma> = sqlQueriesService
                    .execute("adiantamento-apropriado.sql", Parameter("docEntry",it.docEntry!!))
                    ?.tryGetValues<Soma>() ?: listOf(Soma(BigDecimal.ZERO))
                if(apropriacoes.size > 1)
                    throw Exception("Nao pode ter mais de uma apropriacao para um adiantamento")
                it.apropriado = apropriacoes.firstOrNull()?.soma ?: BigDecimal.ZERO
                it
        }
    }

    fun createAdiantamentoBycontrato(contrato : Contrato, carenciaDias : Int){
        val boletos = this.getByContratoVendaFutura(contrato.DocEntry?.toInt() ?: throw Exception("Contrato sem DocEntry"))
        if(boletos.size > 0)
            throw Exception("Não é possivel emitir boletos para um contrato que ja tem boletos")

        val order = try {
            orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        } catch (e: Exception) {
            throw Exception("O pedido do contrato nao foi encontrado")
        }
        val hanndlePaymentTerms = HandlePaymentTermsLines(
            paymentService.getParcelas(order.paymentGroupCode?: throw Exception("Erro ao pegar da condição de pagamento"))
        )
        hanndlePaymentTerms.calculaVencimentos(contrato,carenciaDias).map {
            val adiantamento = this.adiantamentosVendaFuturaSave(contrato,it)
            try{
                bankplus.geraBoletos(
                    adiantamento.getBPL_IDAssignedToInvoice().toInt(),
                    adiantamento.docEntry ?: throw Exception("Falha ao obter docentry do adiantamento"),
                    0,
                    OrigemBoletoEnum.adiantamento)
            }catch (e : Exception){
                logger.error("Erro ao gerar boleto",e)
            }
        }
    }
}