package br.andrew.sap.services.fiscal

import br.andrew.sap.infrastructure.configurations.fiscal.TaxNfeEnvrioment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class TaxNfeService(val envrioment: TaxNfeEnvrioment, val restTemplate: RestTemplate) {

    private val client = OkHttpClient()
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun consultar(docEntry: Int, tipoDocumento: Int): DocumentoFiscal {
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

    fun onlyePdf(docEntry: Int, tipoDocumento: Int): ByteArray {
        val documento = consultar(docEntry, tipoDocumento)
        val url = "${envrioment.host}/api/v3/${envrioment.base}/invent/docs/consultar/pdf".toHttpUrl()
            .newBuilder()
            .addQueryParameter("batchId", documento.batchId)
            .addQueryParameter("modelo", documento.modelo)
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", envrioment.token)
            .build()
        return client.newCall(request).execute().body!!.bytes()
    }

    // pdf e xml recebem o documento ja consultado: no lote cada nota consulta uma vez so,
    // mesmo quando pede os dois arquivos.
    fun pdf(documento: DocumentoFiscal): ByteArray = extraiArquivo("pdf", arquivo("pdf", documento))

    fun xml(documento: DocumentoFiscal): ByteArray = extraiArquivo("xml", arquivo("xml", documento))

    private fun arquivo(tipo: String, documento: DocumentoFiscal): ByteArray {
        val url = "${envrioment.host}/api/v3/${envrioment.base}/invent/docs/consultar/$tipo".toHttpUrl()
            .newBuilder()
            .addQueryParameter("batchId", documento.batchId)
            .addQueryParameter("modelo", documento.modelo)
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", envrioment.token)
            .build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body?.bytes() ?: ByteArray(0)
            if (!response.isSuccessful) {
                // Corpo so no log: pode trazer detalhe interno da TaxPlus, e a mensagem vai para o
                // erros.txt que o usuario baixa.
                logger.warn("TaxPlus respondeu {} ao buscar o {} do lote {}: {}", response.code, tipo,
                    documento.batchId, String(bytes).take(500))
                throw Exception("TaxPlus respondeu HTTP ${response.code} ao buscar o $tipo")
            }
            return bytes
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaxNfeService::class.java)
        private val json = ObjectMapper().registerModule(KotlinModule.Builder().build())

        /**
         * O consultar/xml nao devolve o arquivo cru como o consultar/pdf: responde JSON
         * (`{"sucesso":true,"menssagem":"Sucesso!","batchId":...,"pdf":null,"xml":"PG5mZVByb2M..."}`)
         * com o nfeProc em base64 no campo `xml`. Conferido em 2026-10-01 com o DocEntry 251441.
         * O mesmo envelope tem campo `pdf`, entao os dois tipos aceitam arquivo cru ou JSON.
         */
        fun extraiArquivo(tipo: String, bytes: ByteArray): ByteArray {
            if (assinaturaValida(tipo, bytes))
                return bytes
            val resposta: TaxPlusArquivoResponse? = try {
                json.readValue(bytes, TaxPlusArquivoResponse::class.java)
            } catch (t: Throwable) {
                null
            }
            if (resposta == null) {
                validaConteudo(tipo, bytes)
                return bytes
            }
            if (resposta.sucesso == false)
                throw Exception("TaxPlus recusou o $tipo: ${resposta.menssagem ?: "sem mensagem"}")
            val conteudo = (if (tipo == "pdf") resposta.pdf else resposta.xml)?.trim()?.takeIf { it.isNotEmpty() }
                ?: throw Exception("TaxPlus respondeu sem o $tipo (${resposta.menssagem ?: "sem mensagem"})")
            val arquivo = if (tipo == "xml" && conteudo.startsWith("<")) conteudo.toByteArray(Charsets.UTF_8)
                else try {
                    java.util.Base64.getMimeDecoder().decode(conteudo)
                } catch (t: IllegalArgumentException) {
                    logger.warn("TaxPlus devolveu o {} fora de base64: {}", tipo, conteudo.take(120))
                    throw Exception("TaxPlus devolveu o $tipo num formato desconhecido")
                }
            validaConteudo(tipo, arquivo)
            return arquivo
        }

        private fun assinaturaValida(tipo: String, bytes: ByteArray): Boolean = when (tipo) {
            "pdf" -> inicio(bytes).startsWith("%PDF")
            "xml" -> inicio(bytes).startsWith("<")
            else -> bytes.isNotEmpty()
        }

        private fun inicio(bytes: ByteArray) = String(bytes.copyOfRange(0, minOf(bytes.size, 200)), Charsets.UTF_8)
            .trimStart('\uFEFF', ' ', '\t', '\r', '\n')

        // A TaxPlus responde 200 com JSON de erro em alguns casos; sem essa checagem o JSON iria
        // para o zip com extensao .pdf/.xml e o usuario so descobriria ao abrir.
        fun validaConteudo(tipo: String, bytes: ByteArray) {
            if (assinaturaValida(tipo, bytes))
                return
            if (bytes.isEmpty())
                throw Exception("TaxPlus devolveu o $tipo vazio")
            logger.warn("TaxPlus nao devolveu um {} valido: {}", tipo, inicio(bytes).take(200))
            throw Exception("TaxPlus nao devolveu um $tipo valido")
        }
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class TaxPlusArquivoResponse(
    val sucesso: Boolean?,
    val menssagem: String?,
    val pdf: String?,
    val xml: String?,
)

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
