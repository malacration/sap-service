package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestQrCode
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class DynamicPixQrCodeService(val restTemplate: RestTemplate,
                              val envrioment: UzziPayEnvrioment) {

    val url = envrioment.host+"/gateway"+path()
    fun path(): String {
        return "/v1/qr-codes/dynamic/due-date"
    }

    fun genereateFor(requestQrCode: RequestQrCode): DataRetonroPixQrCode {
        val conta = requestQrCode.getContaSelecioanda(envrioment.contas)
        val toSign = "post:${path()}?${requestQrCode.externalIdentifier},${requestQrCode.getAmount()}"
        val hash = getHash(conta.privateKey,toSign)
        val request = RequestEntity
            .post(url)
            .header("Authorization","Bearer ${conta.tokenJwt}")
            .header("Transaction-hash",hash)
            .body(requestQrCode)
        return restTemplate.exchange(request, DataRetonroPixQrCode::class.java).body ?:
            throw Exception("Nao retornou qr code")
    }

    private fun getHash(privateKey : String, toSign  : String): String {
        val sb = StringBuilder()
        val hMacSHA256 = Mac.getInstance("HmacSHA256")
        hMacSHA256.init(SecretKeySpec(privateKey.toByteArray(), "HmacSHA256"))
        hMacSHA256.doFinal(toSign.toByteArray()).forEach { sb.append(String.format("%02X", it)) }
        return sb.toString()
    }
}