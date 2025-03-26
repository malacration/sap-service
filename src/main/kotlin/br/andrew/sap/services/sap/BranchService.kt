package br.andrew.sap.services.sap

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BranchService(private val sqlQuerysServices: SqlQueriesService) {

    @Cacheable("branch")
    fun getFilialBy(idVendedor : Int): List<Branch> {
        return sqlQuerysServices.execute("filiais-vendedor.sql",Parameter("vendedor",idVendedor))!!.tryGetValues<Branch>()
    }

    fun searchBranchLimited(): List<Branch> {
        return sqlQuerysServices.execute("search-branch-susten.sql")!!.tryGetValues()
    }
}