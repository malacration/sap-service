package br.andrew.sap.services.journal.importacao

import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoCadastros.LancamentoExistente
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoCadastros.Rateio
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

/**
 * Confere as linhas contra os cadastros do SAP e entre si. Erro bloqueia a importacao; aviso so
 * aparece na previa.
 *
 * Nao confere periodo contabil: periodo fechado o Service Layer recusa no commit, e como o lote e
 * tudo ou nada, nada fica gravado pela metade.
 */
@Component
class ImportacaoLancamentoValidador(private val cadastros: ImportacaoLancamentoCadastros) {

    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun validar(linhas: List<LinhaLancamento>) {
        if (linhas.isEmpty()) return
        avisarRepeticoes(linhas)

        val filiais = cadastros.filiais(linhas.mapNotNull { it.filial }.toSet())
        val contas = cadastros.contas(
            linhas.flatMap { listOf(it.contaDebito, it.contaCredito) }.filter { it.isNotBlank() }.toSet()
        )
        val rateios = cadastros.rateios(linhas.flatMap { listOfNotNull(it.grupoEconomico, it.centroCusto) }.toSet())
        val referencias = cadastros.referencias(linhas.mapNotNull { it.referencia }.toSet())
        val iguais = cadastros.iguais(
            linhas.mapNotNull { it.referencia }.toSet(),
            linhas.mapNotNull { it.dataLancamento }.toSet(),
            linhas.map { it.contaDebito }.filter { it.isNotBlank() }.toSet(),
        )

        linhas.forEach { linha ->
            linha.filial?.let { id ->
                val filial = filiais[id]
                if (filial == null) linha.erros += "A filial $id não existe no SAP."
                else if (filial.inativa) linha.erros += "A filial $id (${filial.nome}) está inativa."
            }

            listOf("débito" to linha.contaDebito, "crédito" to linha.contaCredito)
                .filter { (_, codigo) -> codigo.isNotBlank() }
                .forEach { (lado, codigo) ->
                    val conta = contas[codigo]
                    val data = linha.dataLancamento
                    if (lado == "débito") linha.nomeContaDebito = conta?.nome else linha.nomeContaCredito = conta?.nome
                    when {
                        conta == null -> linha.erros += "A conta de $lado $codigo não existe no plano de contas."
                        conta.titulo -> linha.erros += "A conta de $lado $codigo (${conta.nome}) é conta título; " +
                            "use uma conta analítica."
                        conta.bloqueadaParaManual -> linha.erros += "A conta de $lado $codigo (${conta.nome}) não " +
                            "aceita lançamento manual (conta associada a parceiro)."
                        data != null && conta.congeladaEm(data) ->
                            linha.erros += "A conta de $lado $codigo (${conta.nome}) está congelada em " +
                                "${data.format(formatoData)}."
                    }
                }

            linha.grupoEconomico?.let { conferirRateio(linha, it, rateios[it], 1, "O grupo econômico") }
            linha.centroCusto?.let { conferirRateio(linha, it, rateios[it], 2, "O centro de custo") }

            val igual = iguais.firstOrNull { linha.igualA(it) }
            if (igual != null) {
                linha.lancamentoIgual = igual.lancamento
                linha.numeroLancamentoIgual = igual.numero
                val qual = igual.numero?.let { "nº $it, transação ${igual.lancamento}" } ?: "transação ${igual.lancamento}"
                linha.avisos += "Já existe no SAP um lançamento igual ($qual): mesma referência, data, conta de " +
                    "débito e valor. Importar de novo duplica."
            } else {
                //a referencia sozinha e so informativa: a ordem de producao aparece como Ref1 nos
                //lancamentos da propria ordem, com outras contas
                linha.referencia?.let { referencias[it] }?.let {
                    val outros = if (it.quantidade > 1) " (e em mais ${it.quantidade - 1})" else ""
                    linha.avisos += "A referência ${it.referencia} já está na transação ${it.lancamento}$outros."
                }
            }
        }
    }

    private fun conferirRateio(linha: LinhaLancamento, codigo: String, rateio: Rateio?, dimensao: Int, nome: String) {
        when {
            rateio == null -> linha.erros += "$nome $codigo não existe no SAP."
            rateio.dimensao != dimensao -> linha.erros += "$nome $codigo é da dimensão ${rateio.dimensao}; " +
                "esta coluna é da dimensão $dimensao."
            !rateio.ativo -> linha.erros += "$nome $codigo está inativo."
        }
    }

    private fun avisarRepeticoes(linhas: List<LinhaLancamento>) {
        linhas.groupBy { it.chave() }.values.filter { it.size > 1 }.forEach { iguais ->
            iguais.drop(1).forEach { it.avisos += "Linha idêntica à linha ${iguais.first().linha}." }
        }
        linhas.filter { it.referencia != null }.groupBy { it.referencia }.values.filter { it.size > 1 }.forEach { mesmas ->
            val numeros = mesmas.joinToString(", ") { it.linha.toString() }
            mesmas.forEach { it.avisos += "A referência ${it.referencia} se repete nas linhas $numeros do arquivo." }
        }
    }

    private fun LinhaLancamento.igualA(existente: LancamentoExistente) =
        referencia == existente.referencia && dataLancamento == existente.data &&
            contaDebito == existente.conta && valor != null && valor.compareTo(existente.debito) == 0

    private fun LinhaLancamento.chave() = listOf(
        filial, dataLancamento, contaDebito, contaCredito, valor, historico,
        grupoEconomico, centroCusto, referencia, referencia2, referencia3,
    )
}
