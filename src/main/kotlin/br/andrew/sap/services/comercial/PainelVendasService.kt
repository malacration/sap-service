package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.comercial.VendaDetalhe
import br.andrew.sap.model.comercial.VendaMensal
import br.andrew.sap.model.comercial.VendaProduto
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class PainelVendasService(
    private val sqlQueriesService: SqlQueriesService
) {
    private val formato = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * O agrupamento por mes e feito aqui (nao no SQL): o validador de
     * SQLQueries da SAP nao aceita YEAR()/MONTH() dentro de GROUP BY (nenhuma
     * outra query do projeto usa funcao em GROUP BY - so colunas simples).
     * vendas-periodo.sql so filtra por data/vendedor e devolve as faturas.
     *
     * Os 12 meses sao gerados aqui primeiro (independente de ter fatura ou
     * nao) e so depois preenchidos - group By puro deixava de fora qualquer
     * mes sem nenhuma fatura, entao a tela nunca mostrava os 12 meses fixos.
     */
    fun getTotaisMensais(auth: User, slpCode: Int?): List<VendaMensal> {
        val hoje = LocalDate.now()
        val mesAtual = YearMonth.from(hoje)
        val meses = (11 downTo 0).map { mesAtual.minusMonths(it.toLong()) }
        val faturas = buscarPeriodo(auth, slpCode, meses.first().atDay(1), hoje)
        val porMes = faturas.groupBy { anoMes(it.DocDate) }
        return meses.map { mes ->
            val docs = porMes[mes.year to mes.monthValue] ?: emptyList()
            VendaMensal(
                Ano = mes.year,
                Mes = mes.monthValue,
                Total = docs.sumOf { it.DocTotal },
                Qtde = docs.size
            )
        }
    }

    fun getDetalheMes(auth: User, ano: Int, mes: Int, slpCode: Int?): List<VendaDetalhe> {
        val mesReferencia = YearMonth.of(ano, mes)
        return buscarPeriodo(auth, slpCode, mesReferencia.atDay(1), mesReferencia.atEndOfMonth())
    }

    fun getDetalheMesPorProduto(auth: User, ano: Int, mes: Int, slpCode: Int?): List<VendaProduto> {
        val mesReferencia = YearMonth.of(ano, mes)
        val (vendedor, superVendedor) = resolverVendedor(auth, slpCode)
        val params = paramsPeriodo(vendedor, superVendedor, mesReferencia.atDay(1), mesReferencia.atEndOfMonth())
        return sqlQueriesService.getAll<VendaProduto>("vendas-periodo-por-produto.sql", params)
    }

    private fun buscarPeriodo(auth: User, slpCode: Int?, inicio: LocalDate, fim: LocalDate): List<VendaDetalhe> {
        val (vendedor, superVendedor) = resolverVendedor(auth, slpCode)
        val params = paramsPeriodo(vendedor, superVendedor, inicio, fim)
        return sqlQueriesService.getAll<VendaDetalhe>("vendas-periodo.sql", params)
    }

    private fun paramsPeriodo(vendedor: Int, superVendedor: Int, inicio: LocalDate, fim: LocalDate): List<Parameter> {
        return listOf(
            Parameter("startDate", inicio.format(formato)),
            Parameter("finalDate", fim.format(formato)),
            Parameter("vendedor", vendedor),
            Parameter("superVendedor", superVendedor)
        )
    }

    /** DocDate volta no formato "yyyyMMdd" (mesmo formato usado em PainelIntegradoVendas.DocDate). */
    private fun anoMes(docDate: String?): Pair<Int, Int> {
        val data = LocalDate.parse(docDate, DateTimeFormatter.BASIC_ISO_DATE)
        return data.year to data.monthValue
    }

    /**
     * Vendedor comum ve so o proprio SlpCode (sua identidade de login, quando
     * origin == SalePerson) independente do parametro recebido. Admin/vendedor_admin
     * (superVendedor() == Int.MAX_VALUE) pode informar qualquer slpCode. Mesmo
     * idioma de "SlpCode = :vendedor OR SlpCode < :superVendedor" ja usado em
     * romaneio-sem-saida.sql/contratos-vendafutura.sql.
     */
    private fun resolverVendedor(auth: User, slpCode: Int?): Pair<Int, Int> {
        val superVendedor = auth.superVendedor()
        val vendedor = if(superVendedor == Int.MAX_VALUE)
            slpCode ?: if(auth.origin == UserOriginEnum.SalePerson) auth.getIdInt() else 0
        else
            auth.getIdInt()
        return vendedor to superVendedor
    }
}
