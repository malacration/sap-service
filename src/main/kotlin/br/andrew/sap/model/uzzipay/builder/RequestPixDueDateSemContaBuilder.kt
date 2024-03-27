package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.RequestPixDueDate

class RequestPixDueDateSemContaBuilder(val bussinesPartner: BusinessPartner,
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
    fun comContas(contas : List<ContaUzziPayPix>): RequestPixDueDateBuilder {
        return comConta(contas?.first { it.cnpj==bussinessPlace.cnpjSemMascara() } ?: throw Exception("Conta não encontrada"))
    }

    fun comConta(conta : ContaUzziPayPix): RequestPixDueDateBuilder {
        return RequestPixDueDateBuilder(bussinesPartner, bussinessPlace, document, conta)
    }

    fun build(): List<RequestPixDueDate> {
        if(contas.isEmpty()){
            throw Exception("Nenhuma conta configurada")
        }
        return comContas(contas).build()
    }

}