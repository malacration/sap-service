package br.andrew.sap.services.comercial
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.documents.DownPaymentService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import br.andrew.sap.services.security.AuthService

@Service
class ContratoVendaFuturaService(restTemplate: RestTemplate,
                                 val adiantamentoService : DownPaymentService,
                                 val sqlQueriesService : SqlQueriesService,
                                 env: SapEnvrioment,
                                 authService : AuthService) : EntitiesService<Contrato>(env,restTemplate, authService) {

    val logger = LoggerFactory.getLogger(ContratoVendaFuturaService::class.java)

    override fun path(): String {
        return "/b1s/v1/AR_CONTRATO_FUTURO"
    }

    fun getContratos(auth: User,
                     status : Status,
                     idContrato : Int = -1,
                     filial : Int = -1,
                     cliente : String = "-1"): OData? {

        val idContratoIsFilter = if(idContrato == -1)
            Int.MAX_VALUE
        else
            -1

        val filialIsFilter = if(filial == -1)
            Int.MAX_VALUE
        else
            -1

        val clienteIsFilter = if(cliente == "-1") "~" else ""

        val parameters = listOf(
            Parameter("superVendedor",auth.superVendedor()),
            Parameter("vendedor",auth.id),
            Parameter("status",status.toString()),

            Parameter("idContrato",idContrato),
            Parameter("idContratoIsFilter",idContratoIsFilter),

            Parameter("filial",filial),
            Parameter("filialIsFilter",filialIsFilter),

            Parameter("cliente",cliente),
            Parameter("clienteIsFilter",clienteIsFilter),

        )
        return sqlQueriesService.execute("contratos-vendafutura.sql", parameters)
    }

    /**
     * Contrato do UDO acrescido dos tres campos de exibicao que o UDO nao guarda: nome da
     * filial, nome do vendedor e DocNum do pedido de origem. Na listagem eles vem dos joins
     * da contratos-vendafutura.sql; sem repetir esses joins aqui, o detalhe aberto por link
     * direto (ou de contrato cujo status nao e o filtrado na lista, que por isso nunca esta
     * em memoria) mostrava Filial, Vendedor e Numero do Pedido em branco.
     *
     * A view falhar nao pode derrubar a tela: os tres campos sao rotulo, o contrato em si ja
     * veio. Nesse caso volta o contrato cru, como era antes.
     */
    fun getByIdComCabecalho(id: String): Contrato {
        val contrato = getById(id).tryGetValue<Contrato>()
        val docEntry = contrato.DocEntry ?: return contrato
        val cabecalho = try {
            sqlQueriesService
                .execute("contrato-cabecalho.sql", Parameter("idContrato", docEntry))
                ?.tryGetValues<ContratoCabecalho>()
                ?.firstOrNull()
        } catch (e: Exception) {
            logger.warn("Nao foi possivel carregar o cabecalho do contrato [$docEntry]", e)
            null
        } ?: return contrato

        contrato.Bplname = cabecalho.Bplname
        contrato.SalesEmployeeName = cabecalho.SalesEmployeeName
        contrato.OrderDocNum = cabecalho.OrderDocNum
        return contrato
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

/**
 * Projecao da contrato-cabecalho.sql - so os rotulos que o UDO do contrato nao guarda.
 * ignoreUnknown porque o ObjectMapper do OData mantem o FAIL_ON_UNKNOWN_PROPERTIES ligado:
 * qualquer coluna a mais na view derrubaria o parse da linha inteira.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class ContratoCabecalho(
    val DocEntry : Int?,
    val SalesEmployeeName : String?,
    val OrderDocNum : String?,
    val Bplname : String?
)
