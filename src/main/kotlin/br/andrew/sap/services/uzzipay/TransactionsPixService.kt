package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestQrCode
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TransactionsPixService(val restTemplate: RestTemplate,
                             val envrioment: UzziPayEnvrioment) {

    val url = envrioment.host+path()
    fun path(): String {
        return "/api/transactions"
    }

    fun get(id : String, conta: ContaUzziPayPix): Any? {
        val request = RequestEntity
            .get(url+"/$id")
            .header("Authorization","Bearer ${conta.tokenJwt}")
            .header("X-API-Key","${conta.tokenJwt}")
            .build()
        return restTemplate.exchange(request, DataRetonroPixQrCode::class.java).body ?:
            throw Exception("Nao retornou qr code")
    }

    fun get(id : String): Any? {
        return get(id,envrioment.contas[0])
    }
}