package br.andrew.sap.services.invent

import br.andrew.sap.infrastructure.configurations.invent.BankPlusEnvrioment
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.bankplus.Empresa
import br.andrew.sap.model.sap.documents.Invoice
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class BankPlusService(val envrioment: BankPlusEnvrioment, val restTemplate: RestTemplate) {

    val url = envrioment.host
    fun getBoletosBy(idFilial : String, docEntry : String): List<Boleto> {
        val objType = object: ParameterizedTypeReference<List<Boleto>> () {}
        return getEmpresas()
            .filter { it.codigoDaFilial == idFilial }.flatMap {
                try {
                    restTemplate.exchange(RequestEntity
                        .get("$url/api/v2/${envrioment.base}/cobranca/${it.id}/notafiscal/${docEntry}/boletos")
                        .header("Authorization",envrioment.token)
                        .build(), objType).body ?: listOf()
                } catch (t : HttpClientErrorException){
                    t.printStackTrace()
                    if(!t.responseBodyAsString.contains("Nenhum boleto encontrado"))
                        throw t
                    listOf()
                }
            }
    }
    fun getEmpresas(): List<Empresa> {
        val objType = object: ParameterizedTypeReference<List<Empresa>> () {}
        if(empresas.isEmpty()) {
            empresas = restTemplate.exchange(
                RequestEntity
                    .get("$url/api/v2/${envrioment.base}/cobranca/empresas")
                    .header("Authorization", envrioment.token)
                    .build(),objType)
                .body ?: throw Exception("Nao retornou nenhuma empresa")
        }
        return empresas;
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


    fun getPdf(id : String): ByteArray? {
        return restTemplate.exchange(
            RequestEntity
                .get("$url/api/v2/${envrioment.base}/cobranca/boletos/${id}/pdf")
                .header("Authorization", envrioment.token)
                .build(),ByteArray::class.java).body
    }

    companion object{
        var empresas : List<Empresa> = listOf()
    }

}