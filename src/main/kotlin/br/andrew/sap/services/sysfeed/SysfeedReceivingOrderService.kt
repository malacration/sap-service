package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SysfeedReceivingOrderService(
    private val sqlQueriesService: SqlQueriesService,
    private val supplierService: SysfeedSupplierService,
    @Value("\${sysfeed.recebimento.cod-prod-default:1}") private val defaultCodProd: String
) {
    // dataCorte e usages vem sempre do integrador; sem eles nao ha o que consultar.
    fun getPendingPayloads(
        dataCorte: String,
        usages: List<Int>
    ): List<SysfeedReceivingOrderRequest> {
        val startDate = resolveStartDate(dataCorte)
        return resolveUsages(usages)
            .flatMap { getPendingRows(startDate, it) }
            .map { buildPayload(it) }
    }

    private fun resolveStartDate(dataCorte: String): String {
        val informado = dataCorte.trim()
        if (informado.isBlank()) {
            throw SysfeedReceivingValidationException("dataCorte obrigatoria (use o formato yyyy-MM-dd)")
        }
        return try {
            LocalDate.parse(informado).toString()
        } catch (e: Exception) {
            throw SysfeedReceivingValidationException("dataCorte invalida: $informado (use o formato yyyy-MM-dd)")
        }
    }

    private fun resolveUsages(usages: List<Int>): List<Int> {
        return usages.filter { it > 0 }.distinct().takeIf { it.isNotEmpty() }
            ?: throw SysfeedReceivingValidationException("usage obrigatorio (informe ao menos uma utilizacao maior que zero)")
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
            DataValidade = formatSapDate(pending.DataValidade),
            DataFabricacao = formatSapDate(pending.DataFabricacao),
            DataRegistro = formatSapDate(pending.DataRegistro),
            Placa = pending.Placa?.takeIf { it.isNotBlank() } ?: "SEM PLACA",
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

    private fun formatSapDate(sapDate: String?): String? {
        if (sapDate.isNullOrBlank()) return null
        return try {
            val date = LocalDate.parse(sapDate.take(10))
            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " 00:00:00"
        } catch (e: Exception) {
            null
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
