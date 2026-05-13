package br.andrew.sap.model

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.RequestPixImmediate
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
class PixRequestAdiantamento(
    val cardCode : String,
    val valor : BigDecimal,
    val idFilial : Int,
    val docEntry : Int? = null,
    val documentTypes : DocumentTypes? = null,
    val ttlSeconds : Int = 86400
) {
    init {
        if ((docEntry == null) != (documentTypes == null)) {
            throw Exception("docEntry e documentTypes devem ser informados juntos")
        }
    }

    fun getImmediateRequest(): RequestPixImmediate {
        return getImmediateRequest(null)
    }

    fun getImmediateRequest(adiantamento: DownPayment?): RequestPixImmediate {
        val conta = contaSelecionada()
        val installmentId = adiantamento?.documentInstallments
            ?.firstOrNull()
            ?.InstallmentId ?: 0
        return RequestPixImmediate(
            createExternalIdentifier(adiantamento, installmentId),
            conta,
            valor,
            conta.cnpj,
            ttlSeconds
        )
    }

    private fun contaSelecionada(): ContaUzziPayPix {
        val env = uzziPayEnvrioment ?: throw Exception("Nenhuma conta configurada")
        return env.getContaBpId(idFilial)
    }

    private fun createExternalIdentifier(adiantamento: DownPayment?, installmentId: Int): String {
        val docType = adiantamento?.docObjectCode?.name ?: documentTypes?.name ?: DocumentTypes.oDownPayments.name
        val safeNum = adiantamento?.docNum
            ?: docEntry?.toString()
            ?: sanitize(cardCode).ifBlank { "ADIANTAMENTO$idFilial" }
        val safeDocEntry = adiantamento?.docEntry ?: docEntry ?: 0
        return "Num$safeNum" +
                "-Entry$safeDocEntry" +
                "-ins:$installmentId" +
                "-$docType" +
                "-${System.currentTimeMillis()}"
    }

    private fun sanitize(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9]"), "")
    }

    companion object {
        private var uzziPayEnvrioment: UzziPayEnvrioment? = null

        fun setUzziPayEnvrioment(uzziPayEnvrioment: UzziPayEnvrioment) {
            if (uzziPayEnvrioment.contas.isNotEmpty()) {
                Companion.uzziPayEnvrioment = uzziPayEnvrioment
            }
        }

        fun clearContasConfiguradas() {
            uzziPayEnvrioment = null
        }
    }
}
