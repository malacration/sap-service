package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.PrazoPagamentoDto
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class PrazoPagamentoService(val sqlQueriesService : SqlQueriesService,) {

    fun path(): String {
        return "/b1s/v1/BusinessPlaces"
    }


    fun getByTabela(idTabela : Int): List<PrazoPagamentoDto>? {
        return sqlQueriesService
            .execute("prazo-por-tabela-preco.sql", Parameter("tabelaPreco",idTabela))
            ?.tryGetValues<PrazoPagamentoDto>()
    }
}