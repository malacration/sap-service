package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.documents.Installment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.BussinessPlaceService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TransactionsPixService(val restTemplate: RestTemplate,
                             val bussinessPlaceService : BussinessPlaceService,
                             val envrioment: UzziPayEnvrioment) {

    val url = envrioment.consultaHost+path()
    fun path(): String {
        return "/api/transactions"
    }

    fun getBy(id : String, conta: ContaUzziPayPix): Transaction {
        val request = RequestEntity
            .get(url+"?reference=$id")
            .header("X-API-Key","${conta.consulta}")
            .build()
        return restTemplate.exchange(request, Transaction::class.java).body ?:
            throw Exception("Nao foi possivel encontrar a transaction")
    }

    fun getContaBy(invoice : Invoice) : ContaUzziPayPix{
        return envrioment
            .getContaByCnpj(bussinessPlaceService.getById(invoice.getBPL_IDAssignedToInvoice())
            .tryGetValue<BussinessPlace>())
    }

    fun getBy(invoice : Invoice, conta : ContaUzziPayPix): List<Transaction> {
        return getBy(invoice.documentInstallments ?: throw Exception("Nao tem parcelas"),conta)
    }

    fun getBy(installment : List<Installment>, conta: ContaUzziPayPix): List<Transaction> {
        return installment.filter { it.U_pix_reference != null }.map { getBy(it,conta) }
    }

    fun getBy(installment : Installment,conta: ContaUzziPayPix): Transaction {
        return getBy(installment.U_pix_reference ?: throw Exception("Nao tem referencia pix"),conta)
    }

    fun getBy(id : String): Transaction {
        return getBy(id,envrioment.contas[0])
    }
}