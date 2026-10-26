package br.andrew.sap.services.fiscal

import br.andrew.sap.infrastructure.configurations.CacheConfig
import br.andrew.sap.infrastructure.configurations.fiscal.BankPlusEnvrioment
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.bankplus.Empresa
import br.andrew.sap.model.sap.documents.Invoice
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Service
class BankPlusService(val envrioment: BankPlusEnvrioment, val restTemplate: RestTemplate,
                      @Value("\${bankplus.timeout-leitura-segundos:60}") timeoutLeitura: Long = 60) {

    val url = envrioment.host

    // GETs com prazo, so para quem pede (comPrazo = true): o lote de NF-e, que baixa boleto de
    // ate 200 notas e nao pode ficar preso numa BankPlus pendurada - o RestTemplate compartilhado
    // nao tem timeout de leitura. Os outros fluxos seguem sem prazo DE PROPOSITO: a baixa de Pix
    // (AccountsReceivableService) lista os boletos para cancelar depois do pagamento e engole a
    // falha; desistir em 60s ali deixaria boleto ativo de parcela ja paga.
    private val leitura = RestTemplate(SimpleClientHttpRequestFactory().also {
        it.setConnectTimeout(Duration.ofSeconds(10))
        it.setReadTimeout(Duration.ofSeconds(timeoutLeitura))
    }).also { it.interceptors.addAll(restTemplate.interceptors) }

    private val logger = LoggerFactory.getLogger(BankPlusService::class.java)
    private fun cliente(comPrazo: Boolean) = if (comPrazo) leitura else restTemplate

    fun getBoletosBy(
        idFilial : String,
        docEntry : String,
        tipoDocumento: OrigemBoletoEnum = OrigemBoletoEnum.notafiscal,
        comPrazo: Boolean = false
    ): List<Boleto> {
        val objType = object: ParameterizedTypeReference<List<Boleto>> () {}
        return getEmpresas(comPrazo)
            .filter { it.codigoDaFilial == idFilial }.flatMap {
                try {
                    cliente(comPrazo).exchange(RequestEntity
                        .get("$url/api/v2/${envrioment.base}/cobranca/${it.id}/${tipoDocumento}/$docEntry/boletos")
                        .header("Authorization",envrioment.token)
                        .build(), objType).body ?: listOf()
                } catch (t : HttpClientErrorException){
                    logger.error("Erro ao pegar boleto",t)
                    if(!t.responseBodyAsString.contains("Nenhum boleto encontrado"))
                        throw t
                    listOf()
                }
            }
    }
    fun getEmpresas(comPrazo: Boolean = false): List<Empresa> {
        val objType = object: ParameterizedTypeReference<List<Empresa>> () {}
        if(empresas.isEmpty()) {
            empresas = cliente(comPrazo).exchange(
                RequestEntity
                    .get("$url/api/v2/${envrioment.base}/cobranca/empresas")
                    .header("Authorization", envrioment.token)
                    .build(),objType)
                .body ?: throw Exception("Nao retornou nenhuma empresa")
        }
        return empresas;
    }

    fun geraBoletos(idFilial : Int, entryId : Int, parcela : Int, tipoDocumento : OrigemBoletoEnum): ByteArray {
        val empresa = getEmpresas().firstOrNull { it.codigoDaFilial == idFilial.toString() } ?:throw Exception("Filial nao encontrada")
        val url = "$url/api/v2/${envrioment.base}/cobranca/${empresa.codigoDaFilial}/${tipoDocumento}/$entryId/boletos?parcela=$parcela&impresso=S"
        return restTemplate.exchange(
            RequestEntity.post(url)
                .header("Authorization", envrioment.token)
                .build(),ByteArray::class.java)
            .body ?: throw Exception("Nao retornou nenhuma empresa")
    }

    fun cancelarBoleto(boleto : Boleto): Any? {
        if(boleto.id == null)
            throw Exception("Boleto nao possui id")
        return restTemplate.exchange(
            RequestEntity
                .patch("$url/api/v2/${envrioment.base}/cobranca/boletos/${boleto.id}/cancelar")
                .header("Authorization", envrioment.token)
                .build(),Any::class.java).body
    }

    fun getBoletosBy(invoice: Invoice): List<Boleto> {
        return getBoletosBy(
            invoice.getBPL_IDAssignedToInvoice(),
            invoice.docEntry?.toString() ?: throw Exception("Doc Entry nao pode estar nulo"))
    }


    fun getPdf(id : String, comPrazo: Boolean = false): ByteArray? {
        return cliente(comPrazo).exchange(
            RequestEntity
                .get("$url/api/v2/${envrioment.base}/cobranca/boletos/${id}/pdf")
                .header("Authorization", envrioment.token)
                .build(),ByteArray::class.java).body
    }

    companion object{
        var empresas : List<Empresa> = listOf()
    }

}

enum class OrigemBoletoEnum{
    notafiscal,
    adiantamento,
    lcm
}
