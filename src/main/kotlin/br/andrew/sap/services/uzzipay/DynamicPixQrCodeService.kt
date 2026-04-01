package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.RequestPixImmediate
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class DynamicPixQrCodeService(val restTemplate: RestTemplate,
                              val envrioment: UzziPayEnvrioment,
                              val authService: UzziPayAuthService) {

    fun url() : String {
        return envrioment.host+"/gateway"+path()
    }
    fun path(): String {
        return "/v1/qr-codes/dynamic"
    }

    fun genereateFor(requestQrCode: RequestPixDueDate): DataRetonroPixQrCode {
        val conta = requestQrCode.getContaSelecioanda(envrioment.contas)
        val toSign = "post:${path()}?${requestQrCode.externalIdentifier},${requestQrCode.getAmount()}"
        val request = RequestEntity
            .post(url()+"/due-date")
            .header("Authorization","Bearer ${authService.getToken()}")
            .header("Transaction-hash",getHash(conta.privateKey,toSign))
            .body(requestQrCode)
        try {
            return restTemplate.exchange(request, DataRetonroPixQrCode::class.java).body
                ?: throw Exception("Nao retornou qr code")
        } catch (ex: RestClientException) {
            throw Exception("Falha de comunicação com a uzzipay", ex)
        }
    }

    fun genereateFor(requestQrCode: RequestPixImmediate): DataRetonroPixQrCode {
        val conta = requestQrCode.getContaSelecioanda(envrioment.contas)
        val toSign = "post:${path()}?${requestQrCode.externalIdentifier},${requestQrCode.getAmount()}"
        val request = RequestEntity
            .post(url()+"/immediate")
            .header("Authorization","Bearer ${authService.getToken()}")
            .header("Transaction-hash",getHash(conta.privateKey,toSign))
            .body(requestQrCode)
        try {
            return restTemplate.exchange(request, DataRetonroPixQrCode::class.java).body
                ?: throw Exception("Nao retornou qr code")
        } catch (ex: RestClientException) {
            throw Exception("Falha de comunicação com a uzzipay", ex)
        }
    }

    private fun getHash(privateKey : String, toSign  : String): String {
        val sb = StringBuilder()
        val hMacSHA256 = Mac.getInstance("HmacSHA256")
        hMacSHA256.init(SecretKeySpec(privateKey.toByteArray(), "HmacSHA256"))
        hMacSHA256.doFinal(toSign.toByteArray()).forEach { sb.append(String.format("%02X", it)) }
        return sb.toString()
    }
}
