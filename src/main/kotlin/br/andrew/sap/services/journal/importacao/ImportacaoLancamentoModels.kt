package br.andrew.sap.services.journal.importacao

import JournalEntry
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Uma linha do CSV ja convertida. Campo que nao converteu fica null e o motivo vai em [erros]:
 * a previa mostra o arquivo inteiro de uma vez, em vez de parar na primeira linha ruim.
 */
class LinhaLancamento(
    val linha: Int,
    val filial: Int?,
    //o @EnableWebMvc tira o Jackson configurado pelo Boot: LocalDate sairia como [2026,10,5]
    @get:JsonIgnore
    val dataLancamento: LocalDate?,
    val contaDebito: String,
    val contaCredito: String,
    val valor: BigDecimal?,
    val historico: String,
    val grupoEconomico: String?,
    val centroCusto: String?,
    val referencia: String?,
    val referencia2: String?,
    val referencia3: String?,
) {
    val erros: MutableList<String> = mutableListOf()
    val avisos: MutableList<String> = mutableListOf()
    /** Lancamento do SAP igual a esta linha (referencia, data, conta de debito e valor): transacao e numero. */
    var lancamentoIgual: Int? = null
    var numeroLancamentoIgual: Int? = null
    //nomes vem do plano de contas na validacao; null quando a conta nao existe
    var nomeContaDebito: String? = null
    var nomeContaCredito: String? = null

    val data: String? get() = dataLancamento?.toString()

    fun temErro(): Boolean = erros.isNotEmpty()

    /** Mesmo lancamento de duas pernas que o readCsv do /journal/save sempre montou. */
    fun paraLancamento(): JournalEntry {
        check(!temErro()) { "Linha $linha com erro não vira lançamento" }
        return JournalEntry(filial!!, contaDebito, contaCredito, valor!!.toDouble(), historico).also { entry ->
            entry.taxDate = dataLancamento.toString()
            entry.ReferenceDate = entry.taxDate
            entry.journalEntryLines.forEach {
                it.costingCode = grupoEconomico
                it.costingCode2 = centroCusto
            }
            entry.Reference = referencia
            entry.Reference2 = referencia2
            entry.Reference3 = referencia3
        }
    }
}

class LeituraCsv(
    val codificacao: String,
    val linhas: List<LinhaLancamento>,
    val erros: List<String>,
)

class ImportacaoAnterior(
    val id: String,
    val usuario: String?,
    val data: String?,
    val hora: String?,
)

class PreviaImportacaoLancamento(
    val arquivo: String?,
    val hash: String,
    val codificacao: String,
    val linhas: List<LinhaLancamento>,
    /** Problemas do arquivo como um todo (vazio, sem cabecalho, linhas demais). */
    val erros: List<String>,
    val importacoesAnteriores: List<ImportacaoAnterior>,
) {
    val totalLinhas: Int = linhas.size
    val totalValor: BigDecimal = linhas.mapNotNull { it.valor }.fold(BigDecimal.ZERO, BigDecimal::add)
    val linhasComErro: Int = linhas.count { it.temErro() }
    val linhasComAviso: Int = linhas.count { it.avisos.isNotEmpty() }
    val linhasJaNoSap: Int = linhas.count { it.lancamentoIgual != null }
    val podeImportar: Boolean = erros.isEmpty() && linhas.isNotEmpty() && linhasComErro == 0
    /** Importar duplicaria lancamentos: so com a confirmacao explicita do usuario (forcarDuplicado). */
    val exigeConfirmacao: Boolean = importacoesAnteriores.isNotEmpty() || linhasJaNoSap > 0
    /** Linhas sem valor ou sem conta ficam fora: nao ha o que somar (e ja aparecem com erro). */
    val resumoPorConta: List<ResumoConta> = resumir(linhas)

    private companion object {
        private class Lado(val conta: String, val nome: String?, val debito: BigDecimal, val credito: BigDecimal)

        fun resumir(linhas: List<LinhaLancamento>): List<ResumoConta> =
            linhas.filter { it.valor != null }
                .flatMap { l ->
                    listOf(
                        Lado(l.contaDebito, l.nomeContaDebito, l.valor!!, BigDecimal.ZERO),
                        Lado(l.contaCredito, l.nomeContaCredito, BigDecimal.ZERO, l.valor),
                    )
                }
                .filter { it.conta.isNotBlank() }
                .groupBy { it.conta }
                .map { (conta, lados) ->
                    ResumoConta(
                        conta, lados.firstNotNullOfOrNull { it.nome },
                        lados.fold(BigDecimal.ZERO) { soma, it -> soma + it.debito },
                        lados.fold(BigDecimal.ZERO) { soma, it -> soma + it.credito },
                        lados.size,
                    )
                }
                .sortedBy { it.conta }
    }
}

/** Quanto o arquivo lanca em uma conta, somando as linhas em que ela e debito ou credito. */
class ResumoConta(val conta: String, val nome: String?, val debito: BigDecimal, val credito: BigDecimal, val linhas: Int) {
    val saldo: BigDecimal = debito - credito
}

/** Lancamento criado para uma linha do arquivo: transacao (JdtNum) e o "Numero" da tela do SAP. */
class LancamentoGerado(val linha: Int, val transacao: Int, val numero: Int?)

/** Uma importacao no historico, com os lancamentos que ela gerou (vazio se nao foram registrados). */
class HistoricoImportacao(
    val id: String,
    val usuario: String?,
    val data: String?,
    val hora: String?,
    val arquivo: String?,
    val linhas: Int?,
    val total: BigDecimal?,
    val forcado: Boolean,
    val lancamentos: List<LancamentoGerado>,
)

class ResultadoImportacaoLancamento(
    val id: String,
    val status: String,
    //transacoes, na ordem das linhas; e o que fica no historico (U_Lancamentos)
    val lancamentos: List<Int>,
    val mensagem: String,
    val porLinha: List<LancamentoGerado> = emptyList(),
)

/**
 * Recusa esperada (arquivo mudou, tem erro, ja foi importado, SAP rejeitou). Fica fora do handler
 * global de proposito: aquele responde 500 e manda alerta no Telegram, e erro de arquivo do usuario
 * nao e incidente.
 */
class ImportacaoLancamentoException(
    val status: HttpStatus,
    override val message: String,
    val previa: PreviaImportacaoLancamento? = null,
) : RuntimeException(message)
