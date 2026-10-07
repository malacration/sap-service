package br.andrew.sap.services.journal.importacao

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Cadastros que a previa confere, lidos pelo sap-odbc em uma consulta por tipo com IN (...) dos
 * codigos do arquivo. O Service Layer nao tem servico de plano de contas neste projeto e o
 * SQLQueries nao aceita lista como parametro: por ele seria uma requisicao por conta.
 */
@Service
class ImportacaoLancamentoCadastros(private val odbc: OdbcClient) {

    class Filial(val id: Int, val nome: String?, val inativa: Boolean)

    class Conta(
        val codigo: String,
        val nome: String?,
        val titulo: Boolean,
        val bloqueadaParaManual: Boolean,
        private val congelada: Boolean,
        private val congeladaDe: LocalDate?,
        private val congeladaAte: LocalDate?,
    ) {
        fun congeladaEm(data: LocalDate): Boolean =
            congelada && (congeladaDe == null || !data.isBefore(congeladaDe)) &&
                (congeladaAte == null || !data.isAfter(congeladaAte))
    }

    class Rateio(val codigo: String, val dimensao: Int, val ativo: Boolean)

    class ReferenciaExistente(val referencia: String, val lancamento: Int, val quantidade: Int)

    /** Perna de debito de um lancamento que ja esta no SAP. */
    class LancamentoExistente(
        val lancamento: Int, val referencia: String, val data: LocalDate, val conta: String, val debito: BigDecimal,
        //"Numero" da tela do SAP; lancamento e o "No transacao" (TransId/JdtNum)
        val numero: Int? = null,
    )

    private val sqlFiliais by lazy { sql("filiais") }
    private val sqlContas by lazy { sql("contas") }
    private val sqlRateios by lazy { sql("rateios") }
    private val sqlReferencias by lazy { sql("referencias") }
    private val sqlIguais by lazy { sql("iguais") }
    private val sqlNumeros by lazy { sql("numeros") }

    fun filiais(ids: Set<Int>): Map<Int, Filial> =
        consultar(sqlFiliais, "filiais", ids).map {
            Filial(it.inteiro("BPLId"), it.textoOuNull("BPLName"), it.textoOuNull("Disabled") == "Y")
        }.associateBy { it.id }

    fun contas(codigos: Set<String>): Map<String, Conta> =
        consultar(sqlContas, "contas", codigos).map {
            Conta(
                codigo = it.texto("AcctCode"),
                nome = it.textoOuNull("AcctName"),
                titulo = it.textoOuNull("Postable") != "Y",
                bloqueadaParaManual = it.textoOuNull("LocManTran") == "Y",
                congelada = it.textoOuNull("FrozenFor") == "Y",
                congeladaDe = it.dataOuNull("FrozenFrom"),
                congeladaAte = it.dataOuNull("FrozenTo"),
            )
        }.associateBy { it.codigo }

    fun rateios(codigos: Set<String>): Map<String, Rateio> =
        consultar(sqlRateios, "codigos", codigos).map {
            Rateio(it.texto("OcrCode"), it.inteiro("DimCode"), it.textoOuNull("Active") != "N")
        }.associateBy { it.codigo }

    fun referencias(referencias: Set<String>): Map<String, ReferenciaExistente> =
        consultar(sqlReferencias, "referencias", referencias).map {
            ReferenciaExistente(it.texto("Referencia"), it.inteiro("TransId"), it.inteiro("Quantidade"))
        }.associateBy { it.referencia }

    fun iguais(referencias: Set<String>, datas: Set<LocalDate>, contasDebito: Set<String>): List<LancamentoExistente> =
        if (referencias.isEmpty() || datas.isEmpty() || contasDebito.isEmpty()) emptyList()
        else referencias.toList().chunked(REFERENCIAS_POR_CONSULTA).flatMap {
            consultarIguais(it, datas.map { d -> d.toString() }, contasDebito.toList())
        }

    /**
     * Os filtros cruzam conjuntos independentes (referencias x datas x contas), entao um bloco pode
     * passar do limite do sap-odbc, que recusa resultado truncado. Ai o bloco e dividido ao meio ate
     * caber; so uma referencia sozinha acima do limite sobe como erro.
     */
    private fun consultarIguais(referencias: List<String>, datas: List<String>, contas: List<String>): List<LancamentoExistente> {
        val linhas = try {
            odbc.consultar(
                sqlIguais, mapOf("referencias" to referencias, "datas" to datas, "contas" to contas), MAXIMO_LINHAS
            ).rows
        } catch (e: OdbcException) {
            if (e.codigo != "resultado_truncado" || referencias.size == 1) throw e
            val metade = referencias.size / 2
            return consultarIguais(referencias.subList(0, metade), datas, contas) +
                consultarIguais(referencias.subList(metade, referencias.size), datas, contas)
        }
        return linhas.map {
            LancamentoExistente(
                it.inteiro("TransId"), it.texto("Referencia"), it.dataOuNull("Data")!!, it.texto("Conta"),
                it.texto("Debito").toBigDecimal(), it.textoOuNull("Numero")?.toBigDecimal()?.intValueExact(),
            )
        }
    }

    /** TransId -> Number (o "Numero" da tela do SAP). */
    fun numeros(transacoes: Set<Int>): Map<Int, Int> =
        consultar(sqlNumeros, "transacoes", transacoes)
            .filter { it["Numero"] != null }
            .associate { it.inteiro("TransId") to it.inteiro("Numero") }

    //IN () vazio e SQL invalido; sem codigo no arquivo nao ha o que consultar
    private fun consultar(sql: String, parametro: String, valores: Set<Any>): List<Map<String, Any?>> =
        if (valores.isEmpty()) emptyList()
        else odbc.consultar(sql, mapOf(parametro to valores.toList()), MAXIMO_LINHAS).rows

    private fun sql(nome: String) = SqlResource.carregar("odbc/importacao-lancamento-$nome.sql")

    private companion object {
        //as consultas de cadastro devolvem no maximo uma linha por codigo do arquivo; a de iguais,
        //uma por perna de debito com a mesma referencia e data
        const val MAXIMO_LINHAS = 5000
        const val REFERENCIAS_POR_CONSULTA = 100
    }
}

private fun Map<String, Any?>.textoOuNull(campo: String): String? = this[campo]?.toString()
private fun Map<String, Any?>.texto(campo: String): String =
    requireNotNull(textoOuNull(campo)) { "Campo $campo ausente na consulta." }
private fun Map<String, Any?>.inteiro(campo: String): Int = texto(campo).toBigDecimal().intValueExact()
//o sap-odbc devolve data como "2026-01-13T00:00"
private fun Map<String, Any?>.dataOuNull(campo: String): LocalDate? =
    textoOuNull(campo)?.take(10)?.let { LocalDate.parse(it) }
