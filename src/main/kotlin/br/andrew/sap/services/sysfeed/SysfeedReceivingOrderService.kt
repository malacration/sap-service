package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.document.PurchaseInvoiceService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SysfeedReceivingOrderService(
    private val purchaseInvoiceService: PurchaseInvoiceService,
    private val sqlQueriesService: SqlQueriesService,
    private val integratorClient: SysfeedIntegratorClient,
    private val configService: SysfeedConfigService,
    private val objectMapper: ObjectMapper,
    private val supplierService: SysfeedSupplierService,
    @Value("\${sysfeed.recebimento.cod-prod-default:1}") private val defaultCodProd: String
) {
    private val logger = LoggerFactory.getLogger(SysfeedReceivingOrderService::class.java)

    fun getPendingPayloads(): List<SysfeedReceivingOrderRequest> {
        val config = configService.get().recebimento
        return getPendingRows(config.startDate, config.usage).map { buildPayload(it) }
    }

    fun getPayloadsByDocEntry(docEntry: Int): List<SysfeedReceivingOrderRequest> {
        val document = purchaseInvoiceService.getById(docEntry).tryGetValue<PurchaseInvoice>()
        return document.toPendings().map { buildPayload(it) }
    }

    fun executePending(): SysfeedReceivingExecutionResult {
        val config = configService.get().recebimento
        logJson(
            "sysfeed_receiving_execution_started",
            mapOf(
                "mode" to "pending",
                "startDate" to config.startDate,
                "usage" to config.usage
            )
        )
        val pendings = getPendingRows(config.startDate, config.usage)

        logJson(
            "sysfeed_receiving_pending_loaded",
            mapOf(
                "documents" to pendings.map { it.DocEntry }.distinct().size,
                "lines" to pendings.size
            )
        )
        val results = mutableListOf<SysfeedReceivingLineResult>()
        pendings.groupBy { it.DocEntry }.forEach { (docEntry, documentLines) ->
            logJson(
                "sysfeed_receiving_document_started",
                mapOf(
                    "docEntry" to docEntry,
                    "lines" to documentLines.size
                )
            )
            val lineResults = send(documentLines)
            val status = aggregateStatus(lineResults)
            updateDocumentStatus(docEntry, status)
            logJson(
                "sysfeed_receiving_document_finished",
                mapOf(
                    "docEntry" to docEntry,
                    "status" to status.sapValue,
                    "sent" to lineResults.count { it.status == SysfeedReceivingStatus.SENT },
                    "duplicated" to lineResults.count { it.status == SysfeedReceivingStatus.DUPLICATED },
                    "errors" to lineResults.count { it.status == SysfeedReceivingStatus.ERROR }
                )
            )
            results.addAll(lineResults)
        }
        return SysfeedReceivingExecutionResult(results).also { result ->
            logJson(
                "sysfeed_receiving_execution_finished",
                mapOf(
                    "mode" to "pending",
                    "lines" to result.lines.size,
                    "sent" to result.sent,
                    "duplicated" to result.duplicated,
                    "errors" to result.errors
                )
            )
        }
    }

    fun executeByDocEntry(docEntry: Int): SysfeedReceivingExecutionResult {
        logJson(
            "sysfeed_receiving_execution_started",
            mapOf(
                "mode" to "docEntry",
                "docEntry" to docEntry
            )
        )
        val document = purchaseInvoiceService.getById(docEntry).tryGetValue<PurchaseInvoice>()
        val results = send(document.toPendings())
        val status = aggregateStatus(results)
        updateDocumentStatus(docEntry, status)
        return SysfeedReceivingExecutionResult(results).also { result ->
            logJson(
                "sysfeed_receiving_execution_finished",
                mapOf(
                    "mode" to "docEntry",
                    "docEntry" to docEntry,
                    "status" to status.sapValue,
                    "lines" to result.lines.size,
                    "sent" to result.sent,
                    "duplicated" to result.duplicated,
                    "errors" to result.errors
                )
            )
        }
    }

    fun send(pendings: List<SysfeedReceivingPending>): List<SysfeedReceivingLineResult> {
        return pendings.map { pending ->
            try {
                val payload = buildPayload(pending)
                supplierService.ensureSupplierSent(pending.CardCode)
                logJson(
                    "sysfeed_receiving_line_payload_built",
                    mapOf(
                        "docEntry" to pending.DocEntry,
                        "lineNum" to pending.LineNum,
                        "itemCode" to pending.ItemCode,
                        "payload" to payload
                    )
                )
                if (integratorClient.receivingOrderExists(payload.NrProducao)) {
                    logJson(
                        "sysfeed_receiving_line_duplicated",
                        mapOf(
                            "docEntry" to pending.DocEntry,
                            "lineNum" to pending.LineNum,
                            "nrProducao" to payload.NrProducao
                        )
                    )
                    SysfeedReceivingLineResult(pending.DocEntry, payload.NrProducao, SysfeedReceivingStatus.DUPLICATED)
                } else {
                    integratorClient.createReceivingOrder(payload)
                    logJson(
                        "sysfeed_receiving_line_sent",
                        mapOf(
                            "docEntry" to pending.DocEntry,
                            "lineNum" to pending.LineNum,
                            "nrProducao" to payload.NrProducao
                        )
                    )
                    SysfeedReceivingLineResult(pending.DocEntry, payload.NrProducao, SysfeedReceivingStatus.SENT)
                }
            } catch (e: Exception) {
                logJson(
                    "sysfeed_receiving_line_error",
                    mapOf(
                        "docEntry" to pending.DocEntry,
                        "lineNum" to pending.LineNum,
                        "nrProducao" to buildNrProducaoOpch(pending.DocEntry, pending.LineNum),
                        "error" to e.message
                    )
                )
                logger.error("Erro ao integrar recebimento OPCH docEntry={}, lineNum={}", pending.DocEntry, pending.LineNum, e)
                SysfeedReceivingLineResult(pending.DocEntry, buildNrProducaoOpch(pending.DocEntry, pending.LineNum), SysfeedReceivingStatus.ERROR, e.message)
            }
        }
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

    private fun aggregateStatus(results: List<SysfeedReceivingLineResult>): SysfeedReceivingStatus {
        if (results.isEmpty()) return SysfeedReceivingStatus.ERROR
        val errors = results.count { it.status == SysfeedReceivingStatus.ERROR }
        val duplicated = results.count { it.status == SysfeedReceivingStatus.DUPLICATED }
        return when {
            errors == results.size -> SysfeedReceivingStatus.ERROR
            errors > 0 -> SysfeedReceivingStatus.PARTIAL
            duplicated == results.size -> SysfeedReceivingStatus.DUPLICATED
            else -> SysfeedReceivingStatus.SENT
        }
    }

    private fun updateDocumentStatus(docEntry: Int, status: SysfeedReceivingStatus) {
        val payload = mapOf("U_sysfeed_status" to status.sapValue)
        purchaseInvoiceService.update(payload, docEntry.toString())
        logJson(
            "sysfeed_receiving_sap_status_updated",
            mapOf(
                "docEntry" to docEntry,
                "status" to status.sapValue
            )
        )
    }

    private fun logJson(event: String, fields: Map<String, Any?>) {
        logger.info(objectMapper.writeValueAsString(mapOf("event" to event) + fields))
    }

    private fun PurchaseInvoice.toPendings(): List<SysfeedReceivingPending> {
        val entry = docEntry ?: throw SysfeedReceivingValidationException("DocEntry da nota de entrada nao informado")
        return DocumentLines.mapIndexed { index, line ->
            SysfeedReceivingPending(
                DocEntry = entry,
                LineNum = line.LineNum ?: index,
                DocNum = docNum,
                CardCode = CardCode,
                Serial = SequenceSerial,
                ItemCode = line.ItemCode,
                Quantity = line.Quantity,
                NrLoteCodigoRecebimento = line.BatchNumbers.firstOrNull()?.BatchNumber,
                CodProd = defaultCodProd,
                SysfeedStatus = null
            )
        }
    }
}

class SysfeedReceivingValidationException(message: String) : RuntimeException(message)

data class SysfeedReceivingExecutionResult(
    val lines: List<SysfeedReceivingLineResult>
) {
    val sent: Int = lines.count { it.status == SysfeedReceivingStatus.SENT }
    val duplicated: Int = lines.count { it.status == SysfeedReceivingStatus.DUPLICATED }
    val errors: Int = lines.count { it.status == SysfeedReceivingStatus.ERROR }
}

data class SysfeedReceivingLineResult(
    val docEntry: Int?,
    val nrProducao: String,
    val status: SysfeedReceivingStatus,
    val error: String? = null
)

enum class SysfeedReceivingStatus(val sapValue: String) {
    SENT("ENVIADO"),
    DUPLICATED("DUPLICADO"),
    ERROR("ERRO"),
    PARTIAL("PARCIAL")
}
