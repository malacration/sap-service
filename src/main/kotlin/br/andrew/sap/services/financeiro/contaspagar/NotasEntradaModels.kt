package br.andrew.sap.services.financeiro.contaspagar

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Listagem de notas fiscais de entrada (OPCH) para o Nota Agil importar como fonte alternativa ao
 * Qive. Somente leitura.
 *
 * Com [docEntries] preenchido a busca e por DocEntry (importacao das notas escolhidas) e o periodo
 * e ignorado; sem ele, [dataInicial] e [dataFinal] (data de lancamento, `yyyy-MM-dd`) sao
 * obrigatorios. A filial ([cnpjFilial]) e sempre obrigatoria e sempre conferida.
 */
data class ListagemNotasEntradaRequest(
    val cnpjFilial: String? = null,
    val dataInicial: String? = null,
    val dataFinal: String? = null,
    /** Opcional: CNPJ (14) ou CPF (11) do fornecedor, com ou sem mascara. */
    val cpfCnpjFornecedor: String? = null,
    /** Opcional: numero da nota (OPCH.Serial). */
    val numeroNota: String? = null,
    val docEntries: List<Int>? = null,
)

data class ListagemNotasEntradaResponse(val notas: List<NotaEntradaListagem>)

data class NotaEntradaListagem(
    val docEntry: Int,
    val docNum: Int?,
    val numero: Long?,
    val serie: String?,
    /** OPCH.Model (ONFM.AbsEntry). */
    val modelo: String?,
    /** NFE, CTE, NFSE pelo modelo (`contas-pagar.modelos.*`); null para outros modelos. */
    val tipo: TipoNotaEntrada?,
    val chaveAcesso: String?,
    /** Data do documento (OPCH.TaxDate), usada como data de emissao. */
    val dataEmissao: String?,
    /** Data de lancamento (OPCH.DocDate). */
    val dataLancamento: String?,
    val filial: Int?,
    val nomeFilial: String?,
    val cnpjFilial: String,
    val cardCode: String?,
    val nomeFornecedor: String?,
    /** So digitos: CNPJ (14) ou, sem ele, CPF (11) da extensao fiscal do documento (PCH12). */
    val cpfCnpjFornecedor: String?,
    /** OPCH.DocCur. */
    val moeda: String?,
    /** OADM.MainCurncy: moeda local da empresa. */
    val moedaLocal: String?,
    /**
     * OPCH.DocTotal: ja liquido da retencao de categoria Fatura. Em moeda local; null (assim como
     * [valorBruto] e [valorLiquido]) quando a nota nao esta na moeda local, ver [problema].
     */
    val valorTotal: BigDecimal?,
    /** Retencoes (PCH5) em moeda local; null, como os valores, fora da moeda local. */
    val retencaoFatura: BigDecimal?,
    val retencaoPagamento: BigDecimal?,
    /** DocTotal + retencao de categoria Fatura. */
    val valorBruto: BigDecimal?,
    /** DocTotal - retencao de categoria Pagamento (o que o fornecedor recebe). */
    val valorLiquido: BigDecimal?,
    val anexosPdf: Int,
    /**
     * Motivo de a nota nao ser importavel; null quando ok. Hoje: moeda diferente da local ou
     * desconhecida (mesma regra da MOEDA_NAO_SUPORTADA da conferencia de pagamento).
     */
    val problema: String? = null,
) {
    companion object {
        fun from(linha: Map<String, Any?>, cnpjFilial: String, tipo: (String?) -> TipoNotaEntrada?): NotaEntradaListagem? {
            val docEntry = linha.inteiroNota("DocEntry") ?: return null
            val total = linha.decimalNota("DocTotal")
            val retFatura = linha.decimalNota("RetencaoFatura") ?: BigDecimal.ZERO
            val retPagamento = linha.decimalNota("RetencaoPagamento") ?: BigDecimal.ZERO
            val modelo = linha.textoNota("Model")
            // So digitos ASCII, como o filtro de fornecedor do SQL (contas-pagar-notas-entrada-periodo.sql).
            val cnpj = linha.textoNota("CnpjFornecedor")?.filter(::digitoAscii)?.takeIf { it.length == 14 }
            val cpf = linha.textoNota("CpfFornecedor")?.filter(::digitoAscii)?.takeIf { it.length == 11 }
            val moeda = linha.textoNota("DocCur")
            val moedaLocal = linha.textoNota("MainCurncy")
            // Valores sao da moeda local: nota em outra moeda nao expoe nenhum valor monetario (nao importavel).
            val problema = when {
                moeda == null || moedaLocal == null -> "Moeda da nota de entrada desconhecida no SAP"
                moeda != moedaLocal -> "Nota de entrada em moeda estrangeira ($moeda); so a moeda local e suportada"
                else -> null
            }
            val valor = total?.takeIf { problema == null }
            return NotaEntradaListagem(
                docEntry = docEntry,
                docNum = linha.inteiroNota("DocNum"),
                numero = linha.decimalNota("Serial")?.let { runCatching { it.longValueExact() }.getOrNull() },
                serie = linha.textoNota("SeriesStr"),
                modelo = modelo,
                tipo = tipo(modelo),
                chaveAcesso = linha.textoNota("U_ChaveAcesso")?.filterNot(Char::isWhitespace)?.uppercase(),
                dataEmissao = linha.textoNota("TaxDate"),
                dataLancamento = linha.textoNota("DocDate"),
                filial = linha.inteiroNota("BPLId"),
                nomeFilial = linha.textoNota("BPLName"),
                cnpjFilial = cnpjFilial,
                cardCode = linha.textoNota("CardCode"),
                nomeFornecedor = linha.textoNota("CardName"),
                cpfCnpjFornecedor = cnpj ?: cpf,
                moeda = moeda,
                moedaLocal = moedaLocal,
                valorTotal = valor?.dinheiroNota(),
                retencaoFatura = retFatura.takeIf { problema == null }?.dinheiroNota(),
                retencaoPagamento = retPagamento.takeIf { problema == null }?.dinheiroNota(),
                valorBruto = valor?.add(retFatura)?.dinheiroNota(),
                valorLiquido = valor?.subtract(retPagamento)?.dinheiroNota(),
                anexosPdf = linha.inteiroNota("AnexosPdf") ?: 0,
                problema = problema,
            )
        }
    }
}

/** Arquivo do anexo da nota (ATC1), baixado pelo Service Layer. */
data class AnexoNotaEntrada(val absEntry: Int, val nomeArquivo: String, val extensao: String) {
    val arquivo: String get() = "$nomeArquivo.$extensao"
}

class NotaEntradaNaoEncontradaException(message: String) : RuntimeException(message)

/** O anexo existe no SAP, mas o Service Layer nao conseguiu entrega-lo (falha tecnica). */
class AnexoIndisponivelException(message: String) : RuntimeException(message)

private fun Map<String, Any?>.textoNota(campo: String): String? = this[campo]?.toString()?.trim()?.ifEmpty { null }
private fun Map<String, Any?>.decimalNota(campo: String): BigDecimal? = textoNota(campo)?.toBigDecimalOrNull()
private fun Map<String, Any?>.inteiroNota(campo: String): Int? =
    decimalNota(campo)?.let { runCatching { it.intValueExact() }.getOrNull() }
private fun BigDecimal.dinheiroNota(): BigDecimal = setScale(2, RoundingMode.HALF_UP)

/** Digito '0'..'9' (Char.isDigit aceita digitos Unicode, que o SQL nao conta). */
internal fun digitoAscii(c: Char): Boolean = c in '0'..'9'
