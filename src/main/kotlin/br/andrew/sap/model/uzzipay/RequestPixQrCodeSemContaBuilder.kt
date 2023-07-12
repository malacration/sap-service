package br.andrew.sap.model.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.partner.Address
import br.andrew.sap.model.partner.BusinessPartner
import java.math.BigDecimal
import java.math.RoundingMode

class RequestPixQrCodeSemContaBuilder(val bussinesPartner: BusinessPartner,
                                      val bussinessPlace: BussinessPlace,
                                      val document : Document
){
    companion object{
        private var contas : List<ContaUzziPayPix> = listOf()
        fun setUzziPayEnvrioment(uzziPayEnvrioment: UzziPayEnvrioment){
            if(uzziPayEnvrioment.contas.isNotEmpty())
                contas = uzziPayEnvrioment.contas
        }
    }
    fun comContas(contas : List<ContaUzziPayPix>): RequestPixQrCodeBuilder {
        return comConta(contas?.first { it.cnpj==bussinessPlace.cnpjSemMascara() } ?: throw Exception("Conta não encontrada"))
    }

    fun comConta(conta : ContaUzziPayPix): RequestPixQrCodeBuilder {
        return RequestPixQrCodeBuilder(bussinesPartner, bussinessPlace, document, conta)
    }

    fun build(): List<RequestPixDueDate> {
        if(contas.isEmpty()){
            throw Exception("Nenhuma conta configurada")
        }
        return comContas(contas).build()
    }

}
class RequestPixQrCodeBuilder(private val bp: BusinessPartner,
                              private val bussinessPlace: BussinessPlace,
                              private val document : Document,
                              private val conta : ContaUzziPayPix) {
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