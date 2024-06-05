package br.andrew.sap.services.bacen

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.bacen.CotacaoMoeda
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*


@Service
class BacenOlindaService(
    private val restTemplate : RestTemplate,
    @Value("\${bacen.host:https://olinda.bcb.gov.br}") private val host : String = "https://olinda.bcb.gov.br",
    private val numDias : Int = 10){

    private val path = "/olinda/servico/PTAX/versao/v1/odata"
    private val formater = SimpleDateFormat("MM-dd-yyyy")

    private val dataInicial : String
        get() {
            return formater.format(Date.from(Date().toInstant().plus((numDias*-1).toLong(), ChronoUnit.DAYS)))
        }

    private val dataFinal : String
        get() {
            return formater.format(Date())
        }

    fun cotacaoMoeda(moeda : String): List<CotacaoMoeda> {
        val function = "/CotacaoMoedaPeriodo(moeda=@moeda,dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?"
        val parametros = "@moeda='$moeda'&@dataInicial='$dataInicial'&@dataFinalCotacao='$dataFinal'&\$top=100&\$filter=tipoBoletim eq 'Fechamento'&\$format=json"
        val fullUrl = host+path+function+parametros
        return restTemplate.getForEntity<OData>(fullUrl).body?.tryGetValues<CotacaoMoeda>() ?: throw Exception("Nao foi possivel recuperar Cotacao da moeda $moeda")
    }

}