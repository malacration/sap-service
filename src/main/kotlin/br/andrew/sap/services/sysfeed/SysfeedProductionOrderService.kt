package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedProductionOrderPending
import br.andrew.sap.model.sysfeed.SysfeedProductionOrderRequest
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SysfeedProductionOrderService(
    private val sqlQueriesService: SqlQueriesService
) {
    private val logger = LoggerFactory.getLogger(SysfeedProductionOrderService::class.java)
    private val outputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Cada ordem e montada isoladamente: uma linha invalida e ignorada e reportada,
    // sem derrubar o lote inteiro das ordens validas.
    fun getPendingPayloads(dataCorte: String): List<SysfeedProductionOrderRequest> {
        return getPendingRows(resolveStartDate(dataCorte)).mapNotNull { pending ->
            try {
                buildPayload(pending)
            } catch (e: Exception) {
                logger.warn("SYSFEED_ORDEM_PRODUCAO_IGNORADA docEntry={} motivo={}", pending.DocEntry, e.message)
                null
            }
        }
    }

    // dataCorte vem sempre do integrador; sem ele nao ha o que consultar.
    private fun resolveStartDate(dataCorte: String): String {
        val informado = dataCorte.trim()
        if (informado.isBlank()) {
            throw SysfeedProductionOrderValidationException("dataCorte obrigatoria (use o formato yyyy-MM-dd)")
        }
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

        // Quantidade e Numeric no SYSFEED (aceita casas decimais) — nao arredondar para inteiro
        // aqui, senao uma OP com quantidade fracionaria (ex.: 44,28) vira 44 antes mesmo de dividir.
        val quantidadeTotal = parseDecimal("PlannedQty", pending.Quantidade)
        validateNotZero("PlannedQty", quantidadeTotal)
        // U_LbrOne_Batelada ja e o NUMERO de bateladas (nao o tamanho); TotalQuantidade/QuantBat
        // sao Integer no SYSFEED, entao este sim e arredondado.
        val quantidadeBateladas = normalizeNumeric(pending.QuantBat ?: "1")
        validateNotZero("U_LbrOne_Batelada", BigDecimal(quantidadeBateladas))
        val tamanhoBatelada = divideQuantity(quantidadeTotal, BigDecimal(quantidadeBateladas))
        val descricao = pending.DescricaoOrdemProducao?.trim()?.takeIf { it.isNotBlank() }
        validateMaxLength("tipoOrdemProducao", "A", 1)
        validateMaxLength("descricaoOrdemProducao", descricao, 200)

        return SysfeedProductionOrderRequest(
            codOrdemProducao = codOrdemProducao,
            codIntOrdemProducao = codOrdemProducao,
            codFormula = codFormula,
            codIntFormula = codFormula,
            // SYSFEED: Quantidade = tamanho da batelada; QuantBat e TotalQuantidade = numero de bateladas.
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
        return parseDecimal("valor", value).setScale(0, RoundingMode.HALF_UP).toPlainString()
    }

    private fun parseDecimal(field: String, value: String): BigDecimal {
        return try {
            BigDecimal(value.replace(",", "."))
        } catch (e: NumberFormatException) {
            throw SysfeedProductionOrderValidationException("$field invalido: $value")
        }
    }

    private fun validateNotZero(field: String, value: BigDecimal) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            throw SysfeedProductionOrderValidationException("$field deve ser maior que zero")
        }
    }

    // Quantidade e Numeric no SYSFEED: mantem ate 4 casas decimais (mesma precisao exibida no
    // "Tam. Bat." do SYSFEED), removendo zeros a direita para nao poluir o valor de OPs sem fracao.
    private fun divideQuantity(totalQuantidade: BigDecimal, quantBat: BigDecimal): String {
        return totalQuantidade
            .divide(quantBat, 4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }

    private fun formatDate(value: String?): String? {
        val date = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            val normalized = if (date.matches(Regex("\\d{8}"))) {
                "${date.substring(0, 4)}-${date.substring(4, 6)}-${date.substring(6, 8)}"
            } else {
                date.take(10)
            }
            LocalDate.parse(normalized).format(outputDateFormatter)
        } catch (e: Exception) {
            // Data invalida na origem nao deve derrubar a ordem: apenas omitimos o campo (opcional) e registramos.
            logger.warn("SYSFEED_ORDEM_PRODUCAO_DATA_INVALIDA valor={} motivo={}", value, e.message)
            null
        }
    }
}

class SysfeedProductionOrderValidationException(message: String) : RuntimeException(message)
