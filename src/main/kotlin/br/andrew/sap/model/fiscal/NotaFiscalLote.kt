package br.andrew.sap.model.fiscal

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.nio.file.Path

/**
 * Documento da tela de lote, como sai de odbc/nota-fiscal-lote.sql: NF-e de saida emitida para
 * a SEFAZ ou adiantamento de venda futura (ver [TipoDocumentoLote]).
 *
 * Os nomes de coluna sao os aliases do SQL. O sap-odbc serializa decimal como string, por
 * isso numero passa sempre por toString/BigDecimal.
 */
class NotaFiscalResumo(
    val tipo: TipoDocumentoLote,
    val categoria: CategoriaDocumentoLote,
    val docEntry: Int,
    val docNum: Int?,
    val numero: Int?,
    val serie: String?,
    // Adiantamento: DocNum do contrato de venda futura. NF: nulo.
    val contrato: Int?,
    // Adiantamento: DocEntry do contrato (U_venda_futura), para o PDF do contrato no lote.
    val contratoDocEntry: Int? = null,
    val data: String?,
    val cardCode: String?,
    val cardName: String?,
    val filialId: Int?,
    val filial: String?,
    val total: BigDecimal?,
    // Primeiro e ultimo vencimento das parcelas (INV6). O ultimo e o DocDueDate.
    val vencimentoInicial: String?,
    val vencimentoFinal: String?,
    val parcelas: Int,
    // A parcela desta linha (INV6 / DPI6): na listagem cada parcela e uma linha; no download e na
    // troca, a primeira. InsTotal = valor original da parcela.
    val parcela: Int? = null,
    val valorParcela: BigDecimal? = null,
    val vencimentoParcela: String? = null,
    val vendedorCodigo: Int?,
    val vendedor: String?,
    // PeyMethod do documento (codigo da OPYM).
    val formaPagamento: String?,
    val chaveAcesso: String?,
    val statusSefazId: Int?,
    val statusSefaz: String?,
    val statusSap: StatusSapNota?,
) {
    // OINV e ODPI tem DocEntry proprio: so tipo + DocEntry identifica o documento. Fica fora do
    // JSON: a tela tem um getter `chave` (string) e copiar o objeto por cima dele quebrava a lista.
    @get:JsonIgnore
    val chave: ChaveDocumentoLote get() = ChaveDocumentoLote(tipo, docEntry)

    companion object {
        fun from(linha: Map<String, Any?>) = NotaFiscalResumo(
            tipo = TipoDocumentoLote.valueOf(linha.texto("Tipo") ?: "NF"),
            categoria = CategoriaDocumentoLote.valueOf(linha.texto("Categoria") ?: linha.texto("Tipo") ?: "NF"),
            docEntry = linha.inteiro("DocEntry") ?: throw IllegalStateException("Linha sem DocEntry"),
            docNum = linha.inteiro("DocNum"),
            numero = linha.inteiro("Numero"),
            serie = linha.texto("Serie"),
            contrato = linha.inteiro("Contrato"),
            contratoDocEntry = linha.inteiro("ContratoDocEntry"),
            data = linha.texto("DocDate")?.take(10),
            cardCode = linha.texto("CardCode"),
            cardName = linha.texto("CardName"),
            filialId = linha.inteiro("BPLId"),
            filial = linha.texto("BPLName"),
            total = linha.texto("DocTotal")?.toBigDecimalOrNull(),
            vencimentoInicial = linha.texto("VencimentoInicial")?.take(10),
            vencimentoFinal = linha.texto("VencimentoFinal")?.take(10),
            parcelas = linha.inteiro("Parcelas") ?: 0,
            parcela = linha.inteiro("Parcela"),
            valorParcela = linha.texto("ValorParcela")?.toBigDecimalOrNull(),
            vencimentoParcela = linha.texto("VencimentoParcela")?.take(10),
            vendedorCodigo = linha.inteiro("SlpCode")?.takeIf { it != -1 },
            vendedor = linha.texto("SlpName"),
            formaPagamento = linha.texto("FormaPagamento"),
            chaveAcesso = linha.texto("ChaveAcesso"),
            statusSefazId = linha.inteiro("StatusSefazId"),
            statusSefaz = linha.texto("StatusSefaz"),
            statusSap = linha.texto("StatusSap")?.let { StatusSapNota.valueOf(it) },
        )

        private fun Map<String, Any?>.texto(campo: String): String? = this[campo]?.toString()?.trim()?.ifEmpty { null }

        private fun Map<String, Any?>.inteiro(campo: String): Int? = texto(campo)?.toBigDecimal()?.toInt()
    }
}

/**
 * NF = NF-e de saida (OINV) emitida para a SEFAZ: DANFE, XML e boleto.
 * AD = adiantamento de venda futura (ODPI com U_venda_futura): nao emite NF-e, so tem boleto.
 */
enum class TipoDocumentoLote { NF, AD }

data class ChaveDocumentoLote(val tipo: TipoDocumentoLote, val docEntry: Int)

/**
 * Para a tela separar os documentos. O tipo (NF/AD) diz a tabela; a categoria separa, dentro das
 * NF, a de venda futura do PADRAO ANTIGO: simples faturamento (CFOP 5922/6922), fatura de
 * reserva com as entregas feitas em cima dela - o SAP nao deixa trocar a forma de pagamento nela.
 */
enum class CategoriaDocumentoLote(val tipo: TipoDocumentoLote) {
    NF(TipoDocumentoLote.NF),
    NF_FUTURA(TipoDocumentoLote.NF),
    AD(TipoDocumentoLote.AD),
}

// Mesma regra do "Status SAP" dos relatorios de nota (sql/alertas, "notas com error").
// DEVOLVIDO so existe no adiantamento: estornado por nota de credito (ODPI nao tem /Cancel).
enum class StatusSapNota { ABERTO, FECHADO, CANCELADO, DEVOLVIDO }

class StatusSefaz(val id: Int, val descricao: String?)

class NotaFiscalLoteFiltro(
    val dataInicial: String? = null,
    val dataFinal: String? = null,
    val filial: List<Int>? = null,
    val cardCode: String? = null,
    val salesPersonCode: Int? = null,
    // Faixa de numero da NF ("Serial"); um lado so tambem vale (de 500 em diante).
    val numeroDe: Int? = null,
    val numeroAte: Int? = null,
    // Por parcela: so as parcelas que vencem no intervalo. Por documento: o documento entra se
    // QUALQUER parcela vence nele (ver odbc/nota-fiscal-lote.sql).
    val vencimentoDe: String? = null,
    val vencimentoAte: String? = null,
    val statusSap: StatusSapNota? = null,
    val statusSefaz: Int? = null,
    // Nulo = todas as categorias.
    val categoria: CategoriaDocumentoLote? = null,
    // Uma linha por documento em vez de uma por parcela: o "adicionar todas do filtro" da tela,
    // que conta e pega documentos (o lote e de documentos).
    val porDocumento: Boolean = false,
)

class NotaFiscalLoteRequest(
    // NF (OINV) e adiantamentos (ODPI) em listas separadas: os DocEntry se repetem entre as tabelas.
    val docEntries: List<Int> = listOf(),
    val adiantamentos: List<Int> = listOf(),
    val pdf: Boolean = true,
    val xml: Boolean = true,
    val boleto: Boolean = false,
    // PDF do contrato de venda futura de cada adiantamento do lote (um por contrato).
    val contrato: Boolean = false,
    // Logo do deploy da tela, em data: URI, para o PDF do contrato (ver ContratoVendaFuturaPdfService).
    val logo: String? = null,
)

// O zip fica num arquivo temporario (ver NotaFiscalLoteService.zip); quem envia apaga.
class NotaFiscalLoteArquivo(
    val nome: String,
    val caminho: Path,
    val notasComErro: Int,
    // Notas sem nenhum boleto no zip (sem boleto na BankPlus ou so cancelado). Nao e erro.
    val notasSemBoleto: Int = 0,
)

class FormaPagamentoLote(val codigo: String, val descricao: String?)

class TrocaFormaPagamentoRequest(
    val formaPagamento: String = "",
    val docEntries: List<Int> = listOf(),
    val adiantamentos: List<Int> = listOf(),
)

enum class SituacaoTrocaFormaPagamento {
    ATUALIZADO,
    // O SAP recusou porque a forma nao estava no cadastro do parceiro: foi vinculada e a troca repetida.
    VINCULADO_E_ATUALIZADO,
    // O documento ja estava com essa forma: nada foi gravado.
    SEM_MUDANCA,
    // O SAP nao confirmou o PATCH (o prazo da troca acabou com ele em andamento, ou a conexao caiu
    // depois do envio): pode ou nao ter gravado.
    SEM_CONFIRMACAO,
    ERRO,
    NAO_ENCONTRADO,
    // NF de venda futura do padrao antigo (CFOP 5922/6922): o SAP recusa trocar a forma, entao nem
    // se tenta. Nada foi gravado.
    NAO_PERMITIDO,
}

class TrocaFormaPagamentoResultado(
    val tipo: TipoDocumentoLote,
    val docEntry: Int,
    val numero: Int?,
    val cardCode: String?,
    val cardName: String? = null,
    // Nulo quando o documento nao foi encontrado.
    val categoria: CategoriaDocumentoLote? = null,
    val formaAnterior: String?,
    val situacao: SituacaoTrocaFormaPagamento,
    val mensagem: String? = null,
    // Saiu de uma forma de boleto e o documento ainda tem boleto ativo na BankPlus: so aviso.
    val boletoAtivo: Boolean = false,
    // A troca terminou (ou nem precisou), mas algo ficou por fazer ou conferir: vinculo da forma ao
    // parceiro que falhou, boleto que nao deu para conferir, prazo que acabou antes das conferencias.
    // A tela usa para nao dar aviso de sucesso; o detalhe esta na mensagem.
    val pendencia: Boolean = false,
)

enum class SituacaoLinhaImportacao {
    ENCONTRADO,
    // Achou pelo nosso numero, mas o "Nro beneficiario" da planilha aponta outra nota/parcela, ou
    // o valor nao bate: entra no lote (o nosso numero manda) com o aviso.
    DIVERGENTE,
    // Nosso numero que a BankPlus nao conhece.
    NAO_ENCONTRADO,
    // A BankPlus conhece o boleto, mas o documento nao aparece na tela (NF sem NF-e transmitida,
    // documento de cancelamento, adiantamento que nao e de venda futura).
    FORA_DA_TELA,
}

class LinhaImportacaoLote(
    // Linha da planilha (1-based, como o Excel mostra).
    val linha: Int,
    val nossoNumero: String,
    val beneficiario: String?,
    val pagador: String?,
    val valor: java.math.BigDecimal?,
    val vencimento: String?,
    // "Situacao" do banco (Normal, Baixado...), como veio.
    val situacaoBanco: String?,
    val tipo: TipoDocumentoLote? = null,
    val docEntry: Int? = null,
    val parcela: Int? = null,
    val situacao: SituacaoLinhaImportacao,
    val mensagem: String? = null,
)

class ImportacaoLoteResultado(
    // Documentos para por no lote (sem repeticao: varias parcelas do mesmo documento viram um).
    val documentos: List<NotaFiscalResumo>,
    val linhas: List<LinhaImportacaoLote>,
)
