package br.andrew.sap.services.romaneio

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.RomaneioPesagem
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RomaneioPesagemService(restTemplate: RestTemplate,
                             env: SapEnvrioment,
                             authService : AuthService) : EntitiesService<RomaneioPesagem>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AMFS_UDO_RETR"
    }


    fun getNotUseInEntrada(pns: List<String?>,page : Pageable = Pageable.ofSize(20)): Page<Any> {
        val crosJoin = "/b1s/v1/\$crossjoin(AMFS_UDO_RETR,PECU_UDO_REGR)"
        val expand = "?\$expand=AMFS_UDO_RETR(\$select=DocNum),PECU_UDO_REGR(\$select=U_NumeroTicket)"
        val filter = "&\$filter=AMFS_UDO_RETR/DocNum nin BusinessPartners/BPFiscalTaxIDCollection/BPCode"

        val skip = (page.pageNumber*page.pageSize).toString()+"&\$inlinecount=allpages"
        val request = RequestEntity
                .get(env.host+crosJoin+expand+filter)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body?.tryGetPageValues() ?: OData().tryGetPageValues()
    }

}