package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.RequestPixDueDate

class RequestPixDueDateSemContaBuilder(val bussinesPartner: BusinessPartner,
                                       val bussinessPlace: BussinessPlace,
                                       val document : Document,
                                       val parcela : List<Int> = listOf(),
                                       val jurosMoraPercent: Double = 0.0
){
    companion object{
        private var contas : List<ContaUzziPayPix> = listOf()
        fun setUzziPayEnvrioment(uzziPayEnvrioment: UzziPayEnvrioment){
            if(uzziPayEnvrioment.contas.isNotEmpty())
                contas = uzziPayEnvrioment.contas
        }

        fun clearContasConfiguradas() {
            contas = listOf()
        }
    }

    fun comContas(contas : List<ContaUzziPayPix>): RequestPixDueDateBuilder {
        return comConta(contas?.firstOrNull { it.cnpj==bussinessPlace.cnpjSemMascara() }
            ?: throw Exception("Conta não encontrada para o CNPJ ${bussinessPlace.cnpjSemMascara()}"))
    }

    fun comConta(conta : ContaUzziPayPix): RequestPixDueDateBuilder {
        return RequestPixDueDateBuilder(bussinesPartner, bussinessPlace, document, conta, parcela, jurosMoraPercent)
    }

    fun parcelasSolicitadas(): List<Installment> {
        val installments = document.documentInstallments ?: return listOf()
        if (parcela.isEmpty()) {
            return installments
        }
        return installments.filter { it.InstallmentId != null && parcela.contains(it.InstallmentId) }
    }

    fun build(): List<RequestPixDueDate> {
        if(contas.isEmpty()){
            throw Exception("Nenhuma conta configurada")
        }
        val parcelasParaGerar = parcelasSemPixValido()
        if(parcelasParaGerar.isEmpty()){
            return listOf()
        }
        val conta = contaSelecionada()
        return RequestPixDueDateBuilder(
            bussinesPartner,
            bussinessPlace,
            document,
            conta,
            parcelasParaGerar,
            jurosMoraPercent
        ).build()
    }

    private fun parcelasSemPixValido(): List<Int> {
        val parcelasIds = if(parcela.isNotEmpty()) {
            parcela
        } else {
            document.documentInstallments
                ?.filter { it.InstallmentId != null }
                ?.map { it.InstallmentId!! } ?: listOf()
        }
        val installments = document.documentInstallments ?: return parcelasIds
        return installments
            .filter { it.InstallmentId != null && parcelasIds.contains(it.InstallmentId) }
            .filter { !it.isPixValido() }
            .map { it.InstallmentId!! }
    }

    private fun contaSelecionada(): ContaUzziPayPix {
        return contas.firstOrNull {
            it.transitoria == bussinessPlace.BPLID.toString() ||
            it.cnpj==bussinessPlace.cnpjSemMascara()
        } ?: throw Exception("Conta não encontrada para o CNPJ ${bussinessPlace.cnpjSemMascara()}")
    }
}
