package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Payer
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import okhttp3.internal.parseCookie
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
        return Payer(
            bp.getCpfCnpj().value,
            bp.cardName?:"Sem Nome",
            bp.emailAddress?:"sememail@windson.com",
            addresse.addressName?:"Sem Nome",
            addresse.County?:"Sem Cidade",
            addresse.State ?: "Sem Estado",
            addresse.ZipCode ?: "sem ZipCode"
        )
    }
}
