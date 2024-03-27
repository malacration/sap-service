package br.andrew.sap.services.invent

import br.andrew.sap.infrastructure.configurations.invent.TaxNfeEnvrioment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class TaxNfeService(val envrioment: TaxNfeEnvrioment, val restTemplate: RestTemplate) {

    fun getBoletosBy(entidadeId : Int, numeroDocumento : Int, tipoDocumento : Int): NfeTest {
        val value = "[{\"entidadeId\": $entidadeId, \"numeroDocumento\": $numeroDocumento, \"tipoDocumento\": $tipoDocumento}]"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${envrioment.host}/api/v2/PRODUCAO/dfe/recepcionar/obterdfe?ArquivoJason=${value}")
            .addHeader("Authorization", "b18d869a-afdf-41a9-b851-f3e86150bca6")
            .build()
        val responseBody = client.newCall(request).execute().body!!.string()
        val mapper = ObjectMapper().registerModule(KotlinModule())
        return mapper.readValue(responseBody, NfeTest::class.java)
    }

    fun onlyePdf(entidadeId : Int, numeroDocumento : Int, tipoDocumento : Int): ByteArray {
        val value = "[{\"entidadeId\": $entidadeId, \"numeroDocumento\": $numeroDocumento, \"tipoDocumento\": $tipoDocumento}]".replace(":","%3A").replace(",","%2C")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${envrioment.host}/api/v2/PRODUCAO/dfe/recepcionar/obterpdfnfe?ArquivoJason=${value}")
            .addHeader("Authorization", "b18d869a-afdf-41a9-b851-f3e86150bca6")
            .build()
        return client.newCall(request).execute().body!!.bytes()
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class NfeTest(
    val menssagem : String?,
    val NumeroLoteNfe : String?,
    val NumeroNfe : String?,
    val SerieNfe : String?,
    val ChaveNfe : String?,
    val ArquivoPdf : String?
){
    fun base() {
        println(String(java.util.Base64.getEncoder().encode(ArquivoPdf!!.toByteArray())))
    }

    var sucesso : Boolean? = false
}