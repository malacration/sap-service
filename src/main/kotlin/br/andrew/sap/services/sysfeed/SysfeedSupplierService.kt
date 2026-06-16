package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sysfeed.SysfeedSupplierPending
import br.andrew.sap.model.sysfeed.SysfeedSupplierRequest
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.abstracts.SqlQueriesService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SysfeedSupplierService(
    private val businessPartnersService: BusinessPartnersService,
    private val sqlQueriesService: SqlQueriesService,
    private val integratorClient: SysfeedIntegratorClient,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(SysfeedSupplierService::class.java)

    fun executePending(): SysfeedSupplierExecutionResult {
        logJson("sysfeed_supplier_execution_started", mapOf("mode" to "pending"))
        val pendings = sqlQueriesService
            .execute("sysfeed-fornecedores-pendentes.sql")
            ?.tryGetValues<SysfeedSupplierPending>()
            ?: emptyList()

        logJson(
            "sysfeed_supplier_pending_loaded",
            mapOf("suppliers" to pendings.size)
        )
        return SysfeedSupplierExecutionResult(pendings.map { send(it) }).also { result ->
            logJson(
                "sysfeed_supplier_execution_finished",
                mapOf(
                    "mode" to "pending",
                    "sent" to result.sent,
                    "errors" to result.errors
                )
            )
        }
    }

    fun executeByCardCode(cardCode: String): SysfeedSupplierLineResult {
        logJson(
            "sysfeed_supplier_execution_started",
            mapOf(
                "mode" to "cardCode",
                "cardCode" to cardCode
            )
        )
        val supplier = businessPartnersService.getById("'$cardCode'").tryGetValue<BusinessPartner>()
        return send(supplier.toPending()).also { result ->
            logJson(
                "sysfeed_supplier_execution_finished",
                mapOf(
                    "mode" to "cardCode",
                    "cardCode" to cardCode,
                    "status" to result.status.sapValue,
                    "error" to result.error
                )
            )
        }
    }

    fun ensureSupplierSent(cardCode: String) {
        val result = executeByCardCode(cardCode)
        if (result.status != SysfeedSupplierStatus.SENT) {
            throw SysfeedSupplierException(result.error ?: "Erro ao enviar fornecedor $cardCode ao Sigafran")
        }
        logJson(
            "sysfeed_supplier_ensured_for_receiving_order",
            mapOf("cardCode" to cardCode)
        )
    }

    fun getSupplier(identifier: String): String = integratorClient.getSupplier(identifier)

    fun getSuppliers(): String = integratorClient.getSuppliers()

    fun send(pending: SysfeedSupplierPending): SysfeedSupplierLineResult {
        return try {
            val payload = buildPayload(pending)
            logJson(
                "sysfeed_supplier_payload_built",
                mapOf(
                    "cardCode" to pending.CardCode,
                    "payload" to payload
                )
            )
            integratorClient.createSupplier(payload)
            updateSupplierStatus(pending.CardCode, SysfeedSupplierStatus.SENT)
            SysfeedSupplierLineResult(pending.CardCode, SysfeedSupplierStatus.SENT)
        } catch (e: Exception) {
            logger.error("Erro ao enviar fornecedor {} ao Sigafran", pending.CardCode, e)
            runCatching { updateSupplierStatus(pending.CardCode, SysfeedSupplierStatus.ERROR) }
            SysfeedSupplierLineResult(pending.CardCode, SysfeedSupplierStatus.ERROR, e.message)
        }
    }

    fun buildPayload(pending: SysfeedSupplierPending): SysfeedSupplierRequest {
        val cardCode = pending.CardCode.trim()
        val cardName = pending.CardName.trim()
        if (cardCode.isBlank()) {
            throw SysfeedSupplierException("cardCode obrigatorio")
        }
        if (cardName.isBlank()) {
            throw SysfeedSupplierException("cardName obrigatorio")
        }
        validateMinimumCodFornecedor(cardCode)
        return SysfeedSupplierRequest(
            cardCode = cardCode,
            cardName = cardName,
            cnpj = onlyDigits(pending.Cnpj),
            inscricaoEstadual = pending.InscricaoEstadual?.takeIf { it.isNotBlank() } ?: "0",
            endereco = pending.Endereco?.takeIf { it.isNotBlank() },
            cep = onlyDigits(pending.Cep),
            telefone = onlyDigits(pending.Telefone),
            status = "ATIVO"
        )
    }

    fun extractCodFornecedor(cardCode: String): String {
        val code = cardCode.trim().uppercase()
        if (!code.startsWith("FOR")) {
            throw SysfeedSupplierException("CardCode do fornecedor deve iniciar com FOR: $cardCode")
        }
        val number = code.removePrefix("FOR")
        if (!number.matches(Regex("\\d+"))) {
            throw SysfeedSupplierException("CardCode do fornecedor deve conter apenas numeros apos FOR: $cardCode")
        }
        return number.trimStart('0').ifBlank { "0" }
    }

    private fun validateMinimumCodFornecedor(cardCode: String) {
        val codFornecedor = extractCodFornecedor(cardCode).toInt()
        if (codFornecedor < 32) {
            throw SysfeedSupplierException("Fornecedor $cardCode fora do intervalo permitido para Sysfeed: codigo menor que 32")
        }
    }

    private fun updateSupplierStatus(cardCode: String, status: SysfeedSupplierStatus) {
        businessPartnersService.update(mapOf("U_sysfeed_status" to status.sapValue), "'$cardCode'")
        logJson(
            "sysfeed_supplier_sap_status_updated",
            mapOf(
                "cardCode" to cardCode,
                "status" to status.sapValue
            )
        )
    }

    private fun BusinessPartner.toPending(): SysfeedSupplierPending {
        val address = getAddresses().firstOrNull()
        val tax = getBPFiscalTaxIDCollection()?.firstOrNull()
        return SysfeedSupplierPending(
            CardCode = cardCode ?: throw SysfeedSupplierException("cardCode obrigatorio"),
            CardName = cardName ?: throw SysfeedSupplierException("cardName obrigatorio"),
            Cnpj = tax?.TaxId0 ?: tax?.TaxId4,
            InscricaoEstadual = tax?.TaxId1,
            Endereco = listOfNotNull(address?.Street, address?.StreetNo, address?.Block, address?.City, address?.State)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .takeIf { it.isNotBlank() },
            Cep = address?.ZipCode,
            Telefone = phone1 ?: phone2,
            SysfeedStatus = null
        )
    }

    private fun onlyDigits(value: String?): String? = value
        ?.replace("\\D".toRegex(), "")
        ?.takeIf { it.isNotBlank() }

    private fun logJson(event: String, fields: Map<String, Any?>) {
        logger.info(objectMapper.writeValueAsString(mapOf("event" to event) + fields))
    }
}

class SysfeedSupplierException(message: String) : RuntimeException(message)

data class SysfeedSupplierExecutionResult(
    val suppliers: List<SysfeedSupplierLineResult>
) {
    val sent: Int = suppliers.count { it.status == SysfeedSupplierStatus.SENT }
    val errors: Int = suppliers.count { it.status == SysfeedSupplierStatus.ERROR }
}

data class SysfeedSupplierLineResult(
    val cardCode: String,
    val status: SysfeedSupplierStatus,
    val error: String? = null
)

enum class SysfeedSupplierStatus(val sapValue: String) {
    SENT("ENVIADO"),
    ERROR("ERRO")
}
