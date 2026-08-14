package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.model.sap.cadastro.BussinessPlace
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Payer
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class RequestPixDueDateBuilder(
    private val bp: BusinessPartner,
    private val document: Document,
    private val conta: ContaUzziPayPix,
    private var parcelas: List<Int> = listOf(),
    private val jurosMoraPercent: Double = 0.0
) {
    fun build(): List<RequestPixDueDate> {
        if(parcelas.isEmpty() && document.documentInstallments != null) {
            parcelas = document.documentInstallments
                ?.filter { it.InstallmentId != null }
                ?.map { it.InstallmentId!! } ?: listOf()
        }
        return document.documentInstallments?.filter { parcelas.contains(it.InstallmentId) }?.map {
            val dueDate = it.dueDate ?: throw Exception("Data de vencimento não informada")
            val dueDateLocal = LocalDate.parse(dueDate)
            val now = LocalDate.now()
            val effectiveDueDate = if (dueDateLocal.isBefore(now)) now.plusDays(1) else dueDateLocal
            val dataReferencia = if (dueDateLocal.isBefore(now)) effectiveDueDate else now
            val juros = if(jurosMoraPercent > 0.0)
                it.calcularJurosSimplesPorDia(jurosMoraPercent, dataReferencia)
            else 0.0
            RequestPixDueDate(
                it.createExternalIdentifier(document),
                conta,
                BigDecimal(it.total + juros).setScale(2, RoundingMode.HALF_EVEN),
                effectiveDueDate.toString(),
                getPayer(),
                conta.cnpj)
        } ?: listOf()
    }

    private fun getPayer() : Payer {
        val addresse = bp.getAddresses().firstOrNull() ?: Address()
        val addressLine = listOf(addresse.Street, addresse.Block, addresse.addressName)
            .firstOrNull { !it.isNullOrBlank() }
            ?: ""
        val city = listOf(addresse.City, addresse.County)
            .firstOrNull { !it.isNullOrBlank() }
            ?: ""

        return Payer(
            bp.getCpfCnpj().value,
            bp.cardName?:"Sem Nome",
            resolveEmail(),
            addressLine,
            city,
            addresse.State ?: "",
            addresse.ZipCode ?: "",
            conta.businessPlace
        )
    }

    private fun resolveEmail(): String {
        return firstValidEmail(bp.emailAddress, conta.businessPlace?.Email, FALLBACK_EMAIL)
            ?: FALLBACK_EMAIL
    }

    companion object {
        private const val FALLBACK_EMAIL = "sememail@windson.com"
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        private fun firstValidEmail(vararg emails: String?): String? {
            return emails
                .mapNotNull { it?.trim() }
                .firstOrNull { EMAIL_REGEX.matches(it) }
        }
    }
}
