package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SysfeedReceivingOrderService(
    private val sqlQueriesService: SqlQueriesService,
    private val configService: SysfeedConfigService,
    private val supplierService: SysfeedSupplierService,
    @Value("\${sysfeed.recebimento.cod-prod-default:1}") private val defaultCodProd: String
) {
    fun getPendingPayloads(): List<SysfeedReceivingOrderRequest> {
        val config = configService.get().recebimento
        return getPendingRows(config.startDate, config.usage).map { buildPayload(it) }
    }

    fun buildPayload(pending: SysfeedReceivingPending): SysfeedReceivingOrderRequest {
        val nrProducao = buildNrProducaoOpch(pending.DocEntry, pending.LineNum)
        validateNumeric("NrProducao", nrProducao, 8)

        val codProd = pending.CodProd?.takeIf { it.isNotBlank() } ?: defaultCodProd
        validateNumeric("CodProd", codProd)

        val pesoNominal = normalizeNumeric(pending.Quantity)
        validateNumeric("PesoNominalNF", pesoNominal)

        return SysfeedReceivingOrderRequest(
            NrProducao = nrProducao,
            CodProd = codProd,
            CodFornecedor = supplierService.extractCodFornecedor(pending.CardCode),
            PesoNominalNF = pesoNominal,
            TipoProdutoRecebimento = "GRANEL",
            NrBag = "0",
            NrNotaFiscal = pending.Serial ?: pending.DocNum,
            NrLoteCodigoRecebimento = pending.NrLoteCodigoRecebimento,
            Placa = pending.Placa,
            RegLido = "NAO"
        )
    }

    fun buildNrProducaoOpch(docEntry: Int, lineNum: Int): String {
        return "$docEntry$lineNum"
    }

    private fun getPendingRows(startDate: String, usage: Int): List<SysfeedReceivingPending> {
        return sqlQueriesService
            .execute(
                "sysfeed-ordens-recebimento-pendentes.sql",
                listOf(
                    Parameter("startDate", startDate),
                    Parameter("usage", usage)
                )
            )
            ?.tryGetValues<SysfeedReceivingPending>()
            ?: emptyList()
    }

    private fun validateNumeric(field: String, value: String, maxLength: Int? = null) {
        if (!value.matches(Regex("\\d+"))) {
            throw SysfeedReceivingValidationException("$field deve ser numerico: $value")
        }
        if (maxLength != null && value.length > maxLength) {
            throw SysfeedReceivingValidationException("$field deve ter no maximo $maxLength digitos: $value")
        }
    }

    private fun normalizeNumeric(value: String): String {
        val decimal = try {
            BigDecimal(value.replace(",", "."))
        } catch (e: NumberFormatException) {
            throw SysfeedReceivingValidationException("Valor numerico invalido: $value")
        }
        return decimal.setScale(0, RoundingMode.HALF_UP).toPlainString()
    }
}

class SysfeedReceivingValidationException(message: String) : RuntimeException(message)
