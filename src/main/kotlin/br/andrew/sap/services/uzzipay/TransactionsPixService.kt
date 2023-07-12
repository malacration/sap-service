package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TransactionsPixService(val restTemplate: RestTemplate,
                             val envrioment: UzziPayEnvrioment) {

    val url = envrioment.consultaHost+path()
    fun path(): String {
        return "/api/transactions"
    }

    fun get(id : String, conta: ContaUzziPayPix): Transaction {
        val request = RequestEntity
            .get(url+"?reference=$id")
            .header("X-API-Key","${conta.consulta}")
            .build()
        return restTemplate.exchange(request, Transaction::class.java).body ?:
            throw Exception("Nao foi possivel encontrar a transaction")
    }

    fun get(id : String): Transaction {
        return get(id,envrioment.contas[0])
    }
}