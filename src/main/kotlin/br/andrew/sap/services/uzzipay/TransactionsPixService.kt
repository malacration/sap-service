package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.BussinessPlaceService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.jvm.Throws

@Service
class TransactionsPixService(val restTemplate: RestTemplate,
                             val bussinessPlaceService : BussinessPlaceService,
                             val authService: UzziPayAuthService,
                             val envrioment: UzziPayEnvrioment) {

    fun url() : String {
        return envrioment.consultaHost+path()
    }
    fun path(): String {
        return "/api/transactions"
    }

    fun getBy(id : String, conta: ContaUzziPayPix): Transaction {
        val request = RequestEntity
            .get(url()+"?reference=$id")
            .header("X-API-Key","${conta.consulta}")
            .build()
        return restTemplate.exchange(request, Transaction::class.java).body ?:
            throw Exception("Nao foi possivel encontrar a transaction")
    }

    fun getContaBy(document : Document) : ContaUzziPayPix{
        return envrioment
            .getContaBpId(document.getBPL_IDAssignedToInvoice().toIntOrNull() ?: throw Exception("Falha a converter para inteiro ID da filial"))
    }

    fun getBy(invoice : Invoice, conta : ContaUzziPayPix): List<Transaction> {
        return getBy(invoice.documentInstallments ?: throw Exception("Nao tem parcelas"),conta)
    }

    fun getBy(installment : List<Installment>, conta: ContaUzziPayPix): List<Transaction> {
        return installment.filter { it.U_pix_reference != null }.map { getBy(it,conta) }
    }

    fun getBy(installment : Installment, conta: ContaUzziPayPix): Transaction {
        return getBy(installment.U_pix_reference ?: throw Exception("Nao tem referencia pix"),conta)
    }

    fun getBy(id : String): Transaction {
        //TODO talvez isso gere erro
        return getBy(id,envrioment.contas[0])
    }
}