package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.schedules.futura.Soma
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.VendorPaymentService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class DownPaymentService(env: SapEnvrioment,
                         val sqlQueriesService: SqlQueriesService,
                         @Value("\${adiantamento-vf.itemcode:none}") val vfItemAdiantamento : String,
                         @Value("\${adiantamento-vf.formaPagamento:none}") vfFormaPagamento : String,
                         restTemplate: RestTemplate,
                         authService: AuthService,
) :
        EntitiesService<Document>(env, restTemplate, authService) {

    val vfFormaPagamento: String? = if(vfFormaPagamento == "none")  null else vfFormaPagamento

    override fun path(): String {
        return "/b1s/v1/DownPayments"
    }

    fun adiantamentosVendaFutura(document: Document, contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        val linhas = listOf(Product(vfItemAdiantamento,paymentInfo.value.toString(),"1"))
        val adiantamento = DownPayment(
            document.CardCode,
            paymentInfo.dueDate.toString(),
            linhas,
            document.getBPL_IDAssignedToInvoice())
        if(vfFormaPagamento != null)
            adiantamento.paymentMethod = vfFormaPagamento
        adiantamento.U_venda_futura = contrato.DocEntry;
        return save(adiantamento).tryGetValue<Document>()
    }

    fun getByContratoVendaFutura(id: Int): List<Document> {
        val filter = Filter(
            Predicate(Cancelled.column, Cancelled.tNO, Condicao.EQUAL),
            Predicate("U_venda_futura",id,Condicao.EQUAL)
        )
        return get(filter).tryGetValues()

    }

    fun adiantamentosAbertos(invoice : Invoice): List<DownPayment> {
        val exp = Exception("Nao foi possivel buscar o adianemtno, entrega de venda sem id contrato")
        val adiantamentoFilter = Filter(
            Predicate("U_venda_futura",invoice.U_venda_futura?: throw exp,Condicao.EQUAL),
            Predicate("CardCode",invoice.CardCode,Condicao.EQUAL),
            Predicate("DownPaymentStatus","so_Open",Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Close,Condicao.EQUAL),
            //Talvez DownPaymentStatus indique se tem saldo a apropriar ou nao
        )
        return get(adiantamentoFilter).tryGetValues<DownPayment>().map{
            val apropriacoes : List<Soma> = sqlQueriesService
                .execute("adiantamento-apropriado.sql", Parameter("docEntry",it.docEntry!!))
                ?.tryGetValues<Soma>() ?: listOf(Soma(BigDecimal.ZERO))
            if(apropriacoes.size > 1)
                throw Exception("Nao pode ter mais de uma apropriacao para um adiantamento")
            it.apropriado = apropriacoes.firstOrNull()?.soma ?: BigDecimal.ZERO
            it
        }
    }
}