package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.Payer
import br.andrew.sap.model.uzzipay.RequestQrCode
import br.andrew.sap.model.uzzipay.Type
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class DynamicPixQrCodeService(val restTemplate: RestTemplate, val envrioment: UzziPayEnvrioment) {

    val url = envrioment.host+"/gateway"+path()
    fun path(): String {
        return "/v1/qr-codes/dynamic/due-date"
    }

    fun genereateFor(requestQrCode: RequestQrCode): Any? {
        val conta = envrioment.contas.first { it.cnpj==requestQrCode.getCnpj() }
        val toSign = "post:${path()}?${requestQrCode.externalIdentifier},${requestQrCode.getAmount()}"
        val hash = getHash(conta.privateKey,toSign)
        val request = RequestEntity
            .post(url)
            .header("Authorization","Bearer ${conta.tokenJwt}")
            .header("Transaction-hash",hash)
            .body(requestQrCode)
        return restTemplate.exchange(request, Any::class.java).body
    }

    fun generateQrCode(id: String, cnpj: String): Any? {
        val conta = envrioment.contas.first { it.cnpj==cnpj }
        val requestQrCode = getMock(id,conta.chavePix)
        val toSign = "post:${path()}?${requestQrCode.externalIdentifier},${requestQrCode.getAmount()}"
        val hash = getHash(conta.privateKey,toSign)
        val request = RequestEntity
            .post(url)
            .header("Authorization","Bearer ${conta.tokenJwt}")
            .header("Transaction-hash",hash)
            .body(requestQrCode)
        return restTemplate.exchange(request, Any::class.java).body
    }

    fun getMock(id : String, accountPix : String) : RequestQrCode{
        return RequestQrCode(
            id,
            accountPix,
            Type.EVP,
            100.00.toBigDecimal(),
            "2024-12-31",
            Payer(
                "01847004261",
                "Andrew",
                "andrewc3po@gmail.com",
                "Rua dos Bobos",
                "São Paulo",
                "SP",
                "12345678"),
            "1")
    }

    private fun getHash(privateKey : String, toSign  : String): String {
        val sb = StringBuilder()
        val hMacSHA256 = Mac.getInstance("HmacSHA256")
        hMacSHA256.init(SecretKeySpec(privateKey.toByteArray(), "HmacSHA256"))
        hMacSHA256.doFinal(toSign.toByteArray()).forEach { sb.append(String.format("%02X", it)) }
        return sb.toString()
    }
}