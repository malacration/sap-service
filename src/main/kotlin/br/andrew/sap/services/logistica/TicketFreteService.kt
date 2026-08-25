package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.logistica.QuantidadeLocalidade
import br.andrew.sap.model.logistica.TicketFreteLocalidade
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class TicketFreteService(private val sqlQueriesService: SqlQueriesService) {

    private val formato = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Ticket medio de frete por localidade: frete total cobrado dividido pela
     * quantidade de notas.
     *
     * A base sao as notas fiscais nao canceladas (OINV) que tem frete cobrado -
     * despesa de codigo 1 (o mesmo que AdditionalExpenses.frete grava no
     * pedido) com valor maior que zero. Nota sem frete fica de fora: ela nao
     * tem frete pra somar e so aumentaria o denominador, derrubando o ticket
     * medio da localidade. A localidade vem da extensao
     * de endereco da propria nota (INV12.U_LocalidadeS), nao do cadastro atual
     * do cliente - assim o relatorio nao muda de resultado quando alguem
     * corrige a localidade do endereco depois de faturar.
     *
     * Nota sem localidade (ou com codigo que nao existe mais em @RO_LOCAIS)
     * fica de fora: nao ha como atribuir o frete dela a nenhuma localidade.
     *
     * Cada linha leva as duas leituras do mesmo frete: por nota (TicketMedio) e
     * por produto vendido (TicketMedioProduto) - quem escolhe qual usar e a tela.
     *
     * Ordenado do maior ticket medio por nota pro menor - a tela reordena quando
     * o usuario troca a metrica.
     */
    fun getTicketMedioPorLocalidade(inicio: LocalDate, fim: LocalDate, filial: Int?): List<TicketFreteLocalidade> {
        val params = listOf(
            Parameter("startDate", inicio.format(formato)),
            Parameter("finalDate", fim.format(formato)),
            Parameter("filial", filial ?: 0),
            Parameter("filialIsFilter", if (filial == null) Int.MAX_VALUE else -1)
        )
        val linhas = sqlQueriesService
            .getAll<TicketFreteLocalidade>("ticket-medio-frete-localidade.sql", params)
        //quantidade vendida vem de uma segunda view, com o mesmo recorte de notas:
        //juntar INV1 (linhas do produto) com INV3 (linhas de despesa) num SELECT so
        //multiplicaria as linhas e inflaria tanto o frete quanto a quantidade
        val quantidades = sqlQueriesService
            .getAll<QuantidadeLocalidade>("quantidade-itens-frete-localidade.sql", params)
            .associateBy({ it.CodLocalidade }, { it.Quantidade })
        linhas.forEach { it.Quantidade = quantidades[it.CodLocalidade] ?: BigDecimal.ZERO }
        return linhas.sortedByDescending { it.TicketMedio }
    }
}
