package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.SerasaProperties
import br.andrew.sap.model.partner.CpfCnpj
import br.andrew.sap.model.serasa.B49C
import br.andrew.sap.model.serasa.retorno.ParseRetorno
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SerasaService(val conf: SerasaProperties,
                    val restTemplate: RestTemplate) {

    fun getByCpf(cpf : String, score : Boolean = false) : Any? {
        val b4 = B49C(CpfCnpj(cpf),score).toString()
        val url = conf.getBase()
        val request = RequestEntity
            .post(url+b4)
            .build()
        val retorno  = restTemplate.exchange(request, String::class.java).body ?: throw Exception("Erro ao consultar Serasa")
        return ParseRetorno(retorno)
    }
}