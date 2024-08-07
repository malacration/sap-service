package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Payer
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import java.math.BigDecimal
import java.math.RoundingMode

class RequestPixDueDateBuilder(private val bp: BusinessPartner,
                               private val bussinessPlace: BussinessPlace,
                               private val document : Document,
                               private val conta : ContaUzziPayPix
) {
    fun build(): List<RequestPixDueDate> {
        return document.documentInstallments?.map {
            RequestPixDueDate(
                it.createExternalIdentifier(document),
                conta,
                BigDecimal(it.total).setScale(2, RoundingMode.HALF_EVEN),
                it.dueDate ?: throw Exception("Data de vencimento não informada"),
                getPayer(),
                bussinessPlace.cnpjSemMascara())
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