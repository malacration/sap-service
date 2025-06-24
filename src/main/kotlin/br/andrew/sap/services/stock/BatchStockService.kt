package br.andrew.sap.services.stock

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service


@Service
open class BatchStockService(
    private val sqlQueriesService : SqlQueriesService
)  {

    fun get(itemCode : String,
            deposito : String,
            exibeLoteVazio : Boolean = false) : OData? {
        return sqlQueriesService.execute("saldoEstoqueComLotes.sql", listOf(
            Parameter("itemCode",itemCode),
            Parameter("deposito",deposito),
            Parameter("exibeLoteVazio",if(exibeLoteVazio) -1 else 0)
        ))
    }
}


