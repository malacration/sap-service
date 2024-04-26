package br.andrew.sap.services.romaneio

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.romaneio.RomaneioPesagem
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.structs.QuerysServices
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RomaneioPesagemServiceSaida(restTemplate: RestTemplate,
                                  env: SapEnvrioment,
                                  val querysServices: QuerysServices,
                                  authService : AuthService) : EntitiesService<RomaneioPesagem>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AMFS_UDO_RETR"
    }


    fun getNotUseInSaida(pns: List<String?>,page : Pageable = Pageable.ofSize(20)): Page<Any> {
        val crosJoin = "/b1s/v1/\$crossjoin(AMFS_UDO_RETR,AGRI_UDO_RMSD)"
        val expand = "?\$expand=AMFS_UDO_RETR(\$select=DocNum),AGRI_UDO_RMSD(\$select=U_NumeroTicket)"
        val filter = "&\$filter=AMFS_UDO_RETR/DocNum nin BusinessPartners/BPFiscalTaxIDCollection/BPCode"

        val skip = (page.pageNumber*page.pageSize).toString()+"&\$inlinecount=allpages"
        val request = RequestEntity
                .get(env.host+crosJoin+expand+filter)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body?.tryGetPageValues(page) ?: OData().tryGetPageValues(page)
    }

    fun romaneisoSemSaida(page: Pageable = Pageable.ofSize(20), parametros: Filter): Page<RomaneioPesagem> {
        val url = env.host+"/b1s/v1/SQLQueries"
        val skip = (page.pageNumber*page.pageSize).toString()
        val sql = parametros.toSql()
        return restTemplate.exchange(RequestEntity
                .get("$url('romaneio-sem-saida.sql')/List?\$skip=${skip}&$sql")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build(), OData::class.java).body!!.tryGetPageValues(page)
    }

}