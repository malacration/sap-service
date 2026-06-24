package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedSupplierPending
import br.andrew.sap.model.sysfeed.SysfeedSupplierRequest
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class SysfeedSupplierService(
    private val sqlQueriesService: SqlQueriesService
) {
    fun getPendingPayloads(): List<SysfeedSupplierRequest> {
        return getPendingRows().map { buildPayload(it) }
    }

    fun getByCardCode(rawCardCode: String): SysfeedSupplierRequest? {
        val cardCode = normalizeCardCode(rawCardCode)
        val row = getRowByCardCode(cardCode) ?: return null
        return buildPayload(row)
    }

    fun normalizeCardCode(rawCardCode: String): String {
        val code = rawCardCode.trim().uppercase()
        val digits = if (code.startsWith("FOR")) code.removePrefix("FOR") else code
        if (digits.isBlank() || !digits.matches(Regex("\\d+"))) {
            throw SysfeedSupplierException("CardCode inválido: $rawCardCode")
        }
        return "FOR" + digits.padStart(7, '0')
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

    private fun getPendingRows(): List<SysfeedSupplierPending> {
        return sqlQueriesService
            .execute("sysfeed-fornecedores-pendentes.sql")
            ?.tryGetValues<SysfeedSupplierPending>()
            ?: emptyList()
    }

    private fun getRowByCardCode(cardCode: String): SysfeedSupplierPending? {
        return sqlQueriesService
            .execute("sysfeed-fornecedor-por-cardcode.sql", Parameter("cardCode", cardCode))
            ?.tryGetValues<SysfeedSupplierPending>()
            ?.firstOrNull()
    }

    private fun onlyDigits(value: String?): String? = value
        ?.replace("\\D".toRegex(), "")
        ?.takeIf { it.isNotBlank() }
}

class SysfeedSupplierException(message: String) : RuntimeException(message)
