package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedProductionOrderPending
import br.andrew.sap.model.sysfeed.SysfeedProductionOrderRequest
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SysfeedProductionOrderService(
    private val sqlQueriesService: SqlQueriesService,
    @Value("\${sysfeed.producao.start-date:2026-04-01}") private val startDate: String
) {
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getPendingPayloads(dataCorte: String? = null): List<SysfeedProductionOrderRequest> {
        return getPendingRows(resolveStartDate(dataCorte)).map { buildPayload(it) }
    }

    private fun resolveStartDate(dataCorte: String?): String {
        val informado = dataCorte?.trim()?.takeIf { it.isNotBlank() } ?: return startDate
        return try {
            LocalDate.parse(informado).toString()
        } catch (e: Exception) {
            throw SysfeedProductionOrderValidationException("dataCorte invalida: $informado (use o formato yyyy-MM-dd)")
        }
    }

    fun buildPayload(pending: SysfeedProductionOrderPending): SysfeedProductionOrderRequest {
        val codOrdemProducao = pending.DocEntry.toString()
        validateNumeric("codOrdemProducao", codOrdemProducao, 10)
        validateNumeric("codIntOrdemProducao", codOrdemProducao, 10)

        val codFormula = pending.CodFormula?.trim().orEmpty()
        validateNumeric("codFormula", codFormula, 10)
        validateNumeric("codIntFormula", codFormula, 10)

        val plannedQty = normalizeNumeric(pending.Quantidade)
        validateNotZero("PlannedQty", plannedQty)
        val tamanhoBatelada = normalizeNumeric(pending.QuantBat ?: pending.Quantidade)
        validateNotZero("U_LbrOne_Batelada", tamanhoBatelada)
        val quantidadeBateladas = divideQuantity(plannedQty, tamanhoBatelada)
        val descricao = pending.DescricaoOrdemProducao
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "ORDEM DE PRODUCAO $codOrdemProducao"
        validateMaxLength("tipoOrdemProducao", "A", 1)
        validateMaxLength("descricaoOrdemProducao", descricao, 200)

        return SysfeedProductionOrderRequest(
            codOrdemProducao = codOrdemProducao,
            codIntOrdemProducao = codOrdemProducao,
            codFormula = codFormula,
            codIntFormula = codFormula,
            quantidade = tamanhoBatelada,
            prioridade = "1",
            totalQuantidade = quantidadeBateladas,
            quantBat = quantidadeBateladas,
            dataEntradaOP = formatDate(pending.DataEntradaOP),
            dataEntregaProducao = formatDate(pending.DataEntregaProducao),
            tipoOrdemProducao = "A",
            descricaoOrdemProducao = descricao
        )
    }

    private fun getPendingRows(startDate: String): List<SysfeedProductionOrderPending> {
        return sqlQueriesService
            .execute(
                "sysfeed-ordens-producao-pendentes.sql",
                listOf(Parameter("startDate", startDate))
            )
            ?.tryGetValues<SysfeedProductionOrderPending>()
            ?: emptyList()
    }

    private fun validateNumeric(field: String, value: String, maxLength: Int) {
        if (!value.matches(Regex("\\d+"))) {
            throw SysfeedProductionOrderValidationException("$field deve ser numerico: $value")
        }
        if (value.length > maxLength) {
            throw SysfeedProductionOrderValidationException("$field deve ter no maximo $maxLength digitos: $value")
        }
    }

    private fun validateMaxLength(field: String, value: String?, maxLength: Int) {
        if (value != null && value.length > maxLength) {
            throw SysfeedProductionOrderValidationException("$field deve ter no maximo $maxLength caracteres")
        }
    }

    private fun normalizeNumeric(value: String): String {
        val decimal = try {
            BigDecimal(value.replace(",", "."))
        } catch (e: NumberFormatException) {
            throw SysfeedProductionOrderValidationException("Valor numerico invalido: $value")
        }
        return decimal.setScale(0, RoundingMode.HALF_UP).toPlainString()
    }

    private fun validateNotZero(field: String, value: String) {
        if (BigDecimal(value) == BigDecimal.ZERO) {
            throw SysfeedProductionOrderValidationException("$field deve ser maior que zero")
        }
    }

    private fun divideQuantity(totalQuantidade: String, quantBat: String): String {
        return BigDecimal(totalQuantidade)
            .divide(BigDecimal(quantBat), 0, RoundingMode.HALF_UP)
            .toPlainString()
    }

    private fun formatDate(value: String?): String? {
        val date = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val normalized = if (date.matches(Regex("\\d{8}"))) {
            "${date.substring(0, 4)}-${date.substring(4, 6)}-${date.substring(6, 8)}"
        } else {
            date.take(10)
        }
        return LocalDate.parse(normalized).format(outputDateFormatter)
    }
}

class SysfeedProductionOrderValidationException(message: String) : RuntimeException(message)
