package br.andrew.sap.services.comercial
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.comercial.PrazoPagamentoDto
import br.andrew.sap.model.payment.PaymentMethodDto
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class FormaPagamentoService(val sqlQueriesService : SqlQueriesService,) {


    fun getByFilial(idBranch : Int, cardCode : String): List<PaymentMethodDto>? {
        return sqlQueriesService
            .execute("forma-pagamento.sql",
                listOf(Parameter("idBranch",idBranch),
                    Parameter("cardCode",cardCode))
            )
            ?.tryGetValues<PaymentMethodDto>()
    }

    /**
     * Formas liberadas para o portal (U_envia_forca), sem filtro de cliente nem de filial: e a
     * lista de onde o cadastro de cliente novo escolhe o que vincular em CRD2. Depois de vinculada,
     * a tela de venda aplica o filtro de filial de sempre (forma-pagamento.sql).
     */
    fun liberadas(): List<PaymentMethodDto> {
        return sqlQueriesService.getAll<PaymentMethodDto>("forma-pagamento-liberadas.sql")
    }
}