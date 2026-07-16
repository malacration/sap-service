package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SysfeedReceivingOrderService(
    private val sqlQueriesService: SqlQueriesService,
    private val supplierService: SysfeedSupplierService,
    @Value("\${sysfeed.recebimento.cod-prod-default:1}") private val defaultCodProd: String
) {
    private val logger = LoggerFactory.getLogger(SysfeedReceivingOrderService::class.java)

    // dataCorte e usages vem sempre do integrador; sem eles nao ha o que consultar.
    // Cada linha e montada isoladamente: uma linha invalida e ignorada e reportada,
    // sem derrubar o lote inteiro das linhas validas.
    fun getPendingPayloads(
        dataCorte: String,
        usages: List<Int>
    ): List<SysfeedReceivingOrderRequest> {
        val startDate = resolveStartDate(dataCorte)
        return resolveUsages(usages)
            .flatMap { getPendingRows(startDate, it) }
            .mapNotNull { pending ->
                try {
                    buildPayload(pending)
                } catch (e: Exception) {
                    logger.warn(
                        "SYSFEED_ORDEM_RECEBIMENTO_IGNORADA docEntry={} lineNum={} motivo={}",
                        pending.DocEntry, pending.LineNum, e.message
                    )
                    null
                }
            }
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

        // PesoNominalNF e Float no SYSFEED (aceita casas decimais) - nao arredondar para inteiro.
        val pesoNominal = parseDecimal("PesoNominalNF", pending.Quantity)

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
        val date = sapDate?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return try {
            // SQL Queries do Service Layer as vezes devolvem a data sem separadores (yyyyMMdd)
            // em vez de yyyy-MM-dd — mesmo caso ja tratado em SysfeedProductionOrderService.
            val normalized = if (date.matches(Regex("\\d{8}"))) {
                "${date.substring(0, 4)}-${date.substring(4, 6)}-${date.substring(6, 8)}"
            } else {
                date.take(10)
            }
            LocalDate.parse(normalized).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " 00:00:00"
        } catch (e: Exception) {
            // Data invalida na origem: omitimos o campo (opcional) e registramos, em vez de falhar silenciosamente.
            logger.warn("SYSFEED_ORDEM_RECEBIMENTO_DATA_INVALIDA valor={} motivo={}", sapDate, e.message)
            null
        }
    }

    // Mantem as casas decimais originais, sem arredondar, removendo apenas zeros a direita
    // (ex.: "10000.000000" vira "10000") - diferente de validateNumeric, que exige inteiro.
    private fun parseDecimal(field: String, value: String): String {
        val decimal = try {
            BigDecimal(value.replace(",", "."))
        } catch (e: NumberFormatException) {
            throw SysfeedReceivingValidationException("$field invalido: $value")
        }
        return decimal.stripTrailingZeros().toPlainString()
    }
}

class SysfeedReceivingValidationException(message: String) : RuntimeException(message)
