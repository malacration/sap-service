package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.partner.Address
import br.andrew.sap.model.partner.BusinessPartner
import java.math.BigDecimal
import java.math.RoundingMode

class RequestQrCodeBuilder(val bussinesPartner: BusinessPartner,
                           val bussinessPlace: BussinessPlace,
                           val document : Document
){

    fun build(): List<RequestQrCode> {
        val payer = getPayer()
        //TODo falta verificar se a parcela esta paga antes de gerar o qrCode
        return document.documentInstallments?.map {
                RequestQrCode(
                    it.createExternalIdentifier(document),
                    "bussinessPlace.cnpj",
                    Type.TaxId,
                    BigDecimal(it.total).setScale(2, RoundingMode.HALF_EVEN),
                    it.dueDate ?: throw Exception("Data de vencimento não informada"),
                    payer,
                    bussinessPlace.cnpjSemMascara())
        } ?: listOf()


    }

    private fun getPayer() : Payer {
        val addresse = bussinesPartner.getAddresses().firstOrNull() ?: Address()
        return Payer(
            bussinesPartner.getCpfCnpj().value,
            bussinesPartner.cardName?:"Sem Nome",
            bussinesPartner.emailAddress?:"sememail@windson.com",
            addresse.addressName?:"Sem Nome",
            addresse.County?:"Sem Cidade",
            addresse.State ?: "Sem Estado",
            addresse.ZipCode ?: "sem ZipCode"
        )
    }

}