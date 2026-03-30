package br.andrew.sap.services.invent

import br.andrew.sap.infrastructure.configurations.invent.TaxNfeEnvrioment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class TaxNfeService(val envrioment: TaxNfeEnvrioment, val restTemplate: RestTemplate) {

    private val client = OkHttpClient()
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun consultar(entidadeId: Int, docEntry: Int, tipoDocumento: Int): DocumentoFiscal {
        val url = "${envrioment.host}/api/v3/${envrioment.base}/invent/docs/consultar".toHttpUrl()
            .newBuilder()
            .addQueryParameter("numeroDocumento", docEntry.toString())
            .addQueryParameter("tipoDocumento", tipoDocumento.toString())
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", envrioment.token)
            .build()
        val body = client.newCall(request).execute().body!!.string()
        val response = mapper.readValue(body, TaxPlusConsultaResponse::class.java)
        return response.documentosFiscais?.firstOrNull()
            ?: throw Exception("Documento fiscal não encontrado para docEntry $docEntry")
    }

    fun onlyePdf(entidadeId: Int, docEntry: Int, tipoDocumento: Int): ByteArray {
        val documento = consultar(entidadeId, docEntry, tipoDocumento)
        val url = "${envrioment.host}/api/v3/${envrioment.base}/invent/docs/consultar/pdf".toHttpUrl()
            .newBuilder()
            .addQueryParameter("entidadeId", entidadeId.toString())
            .addQueryParameter("batchId", documento.batchId)
            .addQueryParameter("modelo", documento.modelo)
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", envrioment.token)
            .build()
        return client.newCall(request).execute().body!!.bytes()
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class TaxPlusConsultaResponse(
    val sucesso: Boolean?,
    val menssagem: String?,
    val documentosFiscais: List<DocumentoFiscal>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
class DocumentoFiscal(
    val batchId: String?,
    val modelo: String?
)

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
