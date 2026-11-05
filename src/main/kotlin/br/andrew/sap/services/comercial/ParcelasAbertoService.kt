package br.andrew.sap.services.comercial
import br.andrew.sap.model.comercial.ParcelasAberto
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ParcelasAbertoService(private val odbc: OdbcClient) {

    private val sql by lazy { SqlResource.carregar("odbc/titulos-inadimplencia-vendedor.sql") }

    /**
     * Parcelas de nota fiscal e boletos de adiantamento de venda futura, vencidos ha mais de tres
     * dias e em aberto, do vendedor, do mais vencido para o menos. Vai pelo sap-odbc (e nao pelo SQLQueries) porque os dois tipos
     * saem de um UNION ALL, que o SQLQueries nao aceita.
     */
    fun getAllBySql(slpCode : Int) : List<ParcelasAberto> {
        val params = mapOf("slpCode" to slpCode, "data" to LocalDate.now().minusDays(3).toString())
        return odbc.consultar(sql, params, MAX_LINHAS).rows
            .map(::paraParcela)
            .sortedWith(compareBy({ it.DueDate }, { it.CardCode }, { it.Serial }, { it.InstlmntID }))
    }

    private fun paraParcela(linha: Map<String, Any?>) = ParcelasAberto(
        BPLName = linha["BPLName"]?.toString() ?: "",
        CardCode = linha["CardCode"].toString(),
        CardName = linha["CardName"]?.toString() ?: "",
        SlpName = linha["SlpName"]?.toString() ?: "",
        Serial = linha["Serial"].toString(),
        InstlmntID = linha["InstlmntID"].toString(),
        InsTotal = BigDecimal(linha["InsTotal"].toString()),
        // o sap-odbc devolve 2026-03-02T00:00; o modelo formata a partir de yyyyMMdd
        DueDate = linha["DueDate"].toString().take(10).replace("-", ""),
        DocTotal = linha["DocTotal"].toString().toDouble(),
        U_StatusCobranca = linha["U_StatusCobranca"]?.toString(),
        U_AgenteCobrador = linha["U_AgenteCobrador"]?.toString(),
        PaidToDate = BigDecimal(linha["PaidToDate"].toString()),
    ).also {
        it.tipo = if (linha["Tipo"] == "AD") "Fatura" else "NF"
        it.contrato = linha["Contrato"]?.toString()
    }

    companion object {
        const val MAX_LINHAS = 10000
    }
}
