package br.andrew.sap.services.comercial
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.comercial.PrazoPagamentoDto
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

    /**
     * Condicoes da tabela para precificar a TROCA de um contrato de venda futura.
     *
     * Diferente de [getByTabela] (tela de venda) em dois pontos, de proposito:
     *  - nao filtra OCTG.U_Rov_EnviarForca: esse filtro decide o que pode ser OFERECIDO numa
     *    venda nova; a condicao do contrato ja foi usada no pedido original e continua valendo
     *    para ele mesmo que tenha deixado de ser oferecida;
     *  - LEFT JOIN no OCTG: a condicao a vista (-1) pode nao ter linha la, e o INNER JOIN a
     *    descartava - contrato de pedido a vista nunca achava o proprio desconto.
     */
    fun getByTabelaParaContrato(idTabela : Int): List<PrazoPagamentoDto> {
        return (sqlQueriesService
            .execute("prazo-contrato-por-tabela-preco.sql", Parameter("tabelaPreco",idTabela))
            ?.tryGetValues<PrazoPagamentoDto>() ?: listOf())
            .map {
                if(it.PymntGroup.isNullOrBlank() && it.GroupNum == "-1")
                    PrazoPagamentoDto(it.GroupNum, "À vista", it.Code, it.ListNum, it.U_desconto, it.U_juros)
                else it
            }
    }
}