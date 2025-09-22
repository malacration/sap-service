package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.document.DownPaymentService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class ContratoVendaFuturaService(restTemplate: RestTemplate,
                                 val adiantamentoService : DownPaymentService,
                                 val sqlQueriesService : SqlQueriesService,
                                 env: SapEnvrioment,
                                 authService : AuthService) : EntitiesService<Contrato>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AR_CONTRATO_FUTURO"
    }

    fun getContratos(auth: User,
                     status : Status,
                     idContrato : Int = -1,
                     filial : Int = -1): OData? {

        val idContratoIsFilter = if(idContrato == -1)
            Int.MAX_VALUE
        else
            -1

        val filialIsFilter = if(filial == -1)
            Int.MAX_VALUE
        else
            -1

        val parameters = listOf(
            Parameter("superVendedor",auth.superVendedor()),
            Parameter("vendedor",auth.id),
            Parameter("status",status.toString()),

            Parameter("idContrato",idContrato),
            Parameter("idContratoIsFilter",idContratoIsFilter),

            Parameter("filial",filial),
            Parameter("filialIsFilter",filialIsFilter),

        )
        return sqlQueriesService.execute("contratos-vendafutura.sql", parameters)
    }

    fun saveOnly(contrato: Contrato): Contrato {
        val resultado = get(Filter(
            "U_orderDocEntry",
            contrato.U_orderDocEntry,Condicao.EQUAL)
        ).tryGetValues<Contrato>()
        return if(resultado.size > 1)
            throw Exception("Nao e possivel criar contrado de vende de pedido que ja tem contrato. Pedido entry [${contrato.U_orderDocEntry}]")
        else if(resultado.size == 1)
            resultado.first()
        else
            save(contrato).tryGetValue<Contrato>()
    }

    fun adiantamentoComplementarVendaFuturaWithoutSave(contrato: Contrato, valor: BigDecimal): Document {
        val boletos = adiantamentoService.getByContratoVendaFutura(
            contrato.DocEntry?: throw Exception("O id do contrato nao pode ser n ullo"),
            OrderBy(mapOf("DocDueDate" to Order.DESC,"DocEntry" to Order.DESC)))
        val dataVencimento = boletos.first().calcularDataDeVencimento()
        return adiantamentoService.adiantamentosVendaFuturaWithoutSave(contrato,PaymentDueDates(valor,dataVencimento.toLocalDate()))
    }

    fun adiantamentosAhCancelar(contrato: Contrato, resultado: BigDecimal): List<Document> {
        if(resultado.compareTo(BigDecimal.ZERO) < 0)
            throw Exception("Forneca o valor do resultado a ser reduzido em modulo")
        val boletos = adiantamentoService.getByContratoVendaFutura(
            contrato.DocEntry?: throw Exception("O id do contrato nao pode ser nullo"),
            OrderBy(mapOf("DocDueDate" to Order.DESC,"DocTotal" to Order.ASC)))
            .filter { it.DocumentStatus == DocumentStatus.bost_Open }
        val boletosParaCancelar = mutableListOf<Document>()
        var valorAcumulado = BigDecimal.ZERO
        for (boleto in boletos) {
            boletosParaCancelar.add(boleto)
            valorAcumulado += BigDecimal(boleto.DocTotal ?: "0")
            if (valorAcumulado >= resultado) {
                break
            }
        }
        return boletosParaCancelar
    }

}