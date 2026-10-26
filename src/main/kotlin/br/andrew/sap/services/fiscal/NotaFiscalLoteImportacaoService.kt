package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.ChaveDocumentoLote
import br.andrew.sap.model.fiscal.ImportacaoLoteResultado
import br.andrew.sap.model.fiscal.LinhaImportacaoLote
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.Normalizer

/**
 * Monta o lote a partir da planilha de titulos do banco (a "Consulta de titulos" do BB, exportada
 * em .xlsx): cabecalho "Nosso numero | Nome pagador | Nro beneficiario | Valor | Vencimento |
 * Situacao", com um bloco de totais acima dele.
 *
 * O nosso numero e a chave: a BankPlus grava o boleto com ele (sem o digito verificador) e com o
 * documento e a parcela de origem (odbc/nota-fiscal-lote-boletos.sql). O "Nro beneficiario" so
 * confere: NF vem "numero parcela" ("005715 2"), adiantamento vem o DocEntry.
 */
@Service
class NotaFiscalLoteImportacaoService(
    val odbc: OdbcClient,
    val lote: NotaFiscalLoteService,
    @Value("\${nota-fiscal.lote.importacao.maximo-linhas:2000}") val maximoLinhas: Int = 2000,
) {

    private val sqlBoletos by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-boletos.sql") }

    private class LinhaPlanilha(val linha: Int, val nossoNumero: String, val beneficiario: String?, val pagador: String?,
                                val valor: BigDecimal?, val vencimento: String?, val situacaoBanco: String?)

    private class Boleto(val tipo: TipoDocumentoLote, val docEntry: Int, val parcela: Int?, val documento: String?,
                         val valor: BigDecimal?, val cancelado: Boolean)

    fun importar(arquivo: ByteArray): ImportacaoLoteResultado {
        if (arquivo.isEmpty())
            throw IllegalArgumentException("Arquivo vazio.")
        val linhas = linhasDaPlanilha(PlanilhaXlsx.ler(arquivo))
        if (linhas.isEmpty())
            throw IllegalArgumentException("Nao achei titulos na planilha: falta a coluna \"Nosso numero\" ou ela esta vazia.")
        if (linhas.size > maximoLinhas)
            throw IllegalArgumentException("A planilha tem ${linhas.size} titulos; o limite e $maximoLinhas.")

        val boletos = boletos(linhas.map { it.nossoNumero }.distinct())
        val achados = linhas.mapNotNull { boletos[it.nossoNumero] }
        val nf = achados.filter { it.tipo == TipoDocumentoLote.NF }.map { it.docEntry }.distinct()
        val ad = achados.filter { it.tipo == TipoDocumentoLote.AD }.map { it.docEntry }.distinct()
        val documentos = if (nf.isEmpty() && ad.isEmpty()) listOf()
            else lote.buscaDocumentos(nf, ad).distinctBy { it.chave }
        val naTela = documentos.map { it.chave }.toSet()

        val resultado = linhas.map { l ->
            val b = boletos[l.nossoNumero]
            when {
                b == null -> l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "nosso numero nao encontrado na BankPlus")
                ChaveDocumentoLote(b.tipo, b.docEntry) !in naTela -> l.resultado(SituacaoLinhaImportacao.FORA_DA_TELA, b,
                    "${if (b.tipo == TipoDocumentoLote.AD) "adiantamento" else "NF"} DocEntry ${b.docEntry} nao aparece na tela " +
                        "(sem NF-e transmitida, cancelamento ou adiantamento que nao e de venda futura)")
                else -> divergencias(l, b).let { avisos ->
                    if (avisos.isEmpty()) l.resultado(SituacaoLinhaImportacao.ENCONTRADO, b)
                    else l.resultado(SituacaoLinhaImportacao.DIVERGENTE, b, avisos.joinToString("; "))
                }
            }
        }
        // Na ordem da planilha.
        val ordem = resultado.mapNotNull { r -> r.tipo?.let { ChaveDocumentoLote(it, r.docEntry!!) } }.distinct()
        val porChave = documentos.associateBy { it.chave }
        return ImportacaoLoteResultado(ordem.mapNotNull { porChave[it] }, resultado)
    }

    private fun LinhaPlanilha.resultado(situacao: SituacaoLinhaImportacao, b: Boleto? = null, mensagem: String? = null) =
        LinhaImportacaoLote(linha, nossoNumero, beneficiario, pagador, valor, vencimento, situacaoBanco,
            b?.tipo, b?.docEntry, b?.parcela, situacao, mensagem)

    private fun divergencias(l: LinhaPlanilha, b: Boleto): List<String> {
        val avisos = mutableListOf<String>()
        if (b.cancelado)
            avisos.add("boleto cancelado na BankPlus")
        val partes = l.beneficiario?.trim()?.split(Regex("\\s+"))?.mapNotNull { it.toIntOrNull() }.orEmpty()
        // O que nao deu para ler nao foi conferido: nao pode sair como ENCONTRADO. NF precisa de
        // numero e parcela; adiantamento, do DocEntry.
        val esperadas = if (b.tipo == TipoDocumentoLote.NF) 2 else 1
        if (partes.size < esperadas)
            avisos.add("Nro beneficiario ${l.beneficiario?.let { "'$it' " } ?: ""}ilegivel: nao conferido")
        if (l.valor == null)
            avisos.add("valor da planilha ilegivel: nao conferido")
        when (b.tipo) {
            TipoDocumentoLote.NF -> {
                val numero = partes.getOrNull(0)
                val parcela = partes.getOrNull(1)
                if (numero != null && b.documento?.toIntOrNull() != null && numero != b.documento.toInt())
                    avisos.add("Nro beneficiario diz NF $numero, a BankPlus diz NF ${b.documento}")
                if (parcela != null && b.parcela != null && parcela != b.parcela)
                    avisos.add("Nro beneficiario diz parcela $parcela, a BankPlus diz parcela ${b.parcela}")
            }
            TipoDocumentoLote.AD -> {
                val docEntry = partes.getOrNull(0)
                if (docEntry != null && docEntry != b.docEntry)
                    avisos.add("Nro beneficiario diz adiantamento $docEntry, a BankPlus diz ${b.docEntry}")
            }
        }
        if (l.valor != null && b.valor != null && l.valor.compareTo(b.valor) != 0)
            avisos.add("valor na planilha ${l.valor}, na BankPlus ${b.valor}")
        return avisos
    }

    // Cabecalho pode se repetir (uma secao por carteira): cada um refaz o mapa de colunas.
    private fun linhasDaPlanilha(planilha: Map<Int, Map<Int, String>>): List<LinhaPlanilha> {
        var colunas: Map<String, Int>? = null
        val linhas = mutableListOf<LinhaPlanilha>()
        for ((numero, celulas) in planilha) {
            val nomes = celulas.mapValues { normaliza(it.value) }
            if (nomes.values.any { it == "nosso numero" }) {
                colunas = nomes.entries.associate { it.value to it.key }
                continue
            }
            val c = colunas ?: continue
            val nosso = celulas[c["nosso numero"]]?.let(::nossoNumero) ?: continue
            linhas.add(LinhaPlanilha(
                linha = numero,
                nossoNumero = nosso,
                beneficiario = celulas[c["nro beneficiario"]],
                pagador = celulas[c["nome pagador"]],
                valor = celulas[c["valor"]]?.let(::valor),
                vencimento = celulas[c["vencimento"]]?.let(::data),
                situacaoBanco = celulas[c["situacao"]],
            ))
        }
        return linhas
    }

    private fun boletos(nossosNumeros: List<String>): Map<String, Boleto> =
        nossosNumeros.chunked(500).flatMap { parte ->
            odbc.consultar(sqlBoletos, mapOf("nossosNumeros" to parte), parte.size * 5).rows
        }.mapNotNull { r ->
            val tipo = when (r["DocType"]?.toString()?.toBigDecimalOrNull()?.toInt()) {
                13 -> TipoDocumentoLote.NF
                203 -> TipoDocumentoLote.AD
                else -> return@mapNotNull null
            }
            val docEntry = r["DocEntry"]?.toString()?.toBigDecimalOrNull()?.toInt() ?: return@mapNotNull null
            r["NossoNumero"].toString() to Boleto(tipo, docEntry, r["Parcela"]?.toString()?.toBigDecimalOrNull()?.toInt(),
                r["Documento"]?.toString(), r["Valor"]?.toString()?.toBigDecimalOrNull(),
                r["Status"]?.toString()?.toBigDecimalOrNull()?.toInt() == STATUS_CANCELADO)
        }.groupBy({ it.first }, { it.second })
            // reemitido: fica o que nao esta cancelado
            .mapValues { (_, lista) -> lista.firstOrNull { !it.cancelado } ?: lista.first() }

    companion object {
        // Status da BankPlus que os relatorios ja tratam como cancelado (ClienteXBoleto.sql: Status <> 5).
        const val STATUS_CANCELADO = 5

        // "35158540000021002-5" -> "35158540000021002": a BankPlus grava sem o digito verificador.
        fun nossoNumero(texto: String): String? =
            texto.substringBefore('-').filter { it.isDigit() }.takeIf { it.length >= 6 }

        // Celula de numero vem "16088.38"; texto digitado pode vir "16.088,38".
        fun valor(texto: String): BigDecimal? {
            val t = texto.replace("R$", "").trim()
            return (if (t.contains(',')) t.replace(".", "").replace(',', '.') else t).toBigDecimalOrNull()
        }

        // Celula de data vem como serial do Excel; texto, como dd/MM/yyyy.
        fun data(texto: String): String? = texto.toDoubleOrNull()?.let { PlanilhaXlsx.dataDoExcel(it).toString() }
            ?: runCatching { java.time.LocalDate.parse(texto, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() }.getOrNull()

        fun normaliza(texto: String): String =
            Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().replace(Regex("\\s+"), " ").trim()
    }
}
