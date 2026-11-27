package br.andrew.sap.services.financeiro.contaspagar

import java.math.BigDecimal

/**
 * Pedido de conferencia de pagamento de uma parcela de nota fiscal de ENTRADA (contas a pagar).
 *
 * Chega do Nota Agil, que por sua vez recebeu do SoftExpert (processo FXP-NA). Numero da nota e
 * da parcela vem como texto, como o SE manda: a interpretacao (zeros a esquerda, faixa do
 * `Serial` do SAP) fica aqui, junto da regra de negocio.
 */
data class ConsultaPagamentoParcelaRequest(
    /** CNPJ da filial tomadora (OBPL / BusinessPlaces.FederalTaxID), com ou sem mascara. */
    val cnpjFilial: String? = null,
    /** CNPJ do fornecedor prestador (CRD7.TaxId0 de parceiro CardType = 'S'), com ou sem mascara. */
    val cnpjFornecedor: String? = null,
    /**
     * Numero da nota fiscal (OPCH.Serial, INTEGER). NFS-e com mais de 9 digitos significativos e
     * lancada no SAP com os 9 ultimos (o prefixo de ano cai): ver [ConsultaPagamentoParcelaService].
     */
    val numeroNota: String? = null,
    /**
     * Serie da nota (OPCH.SeriesStr). NFe/CTe: filtro (comparada sem zeros a esquerda). NFS-e:
     * serie do RPS/DPS, usada so para desempatar e so contra SeriesStr preenchida.
     */
    val serieNota: String? = null,
    /** Numero da parcela (PCH6.InstlmntID). Opcional quando a nota tem uma unica parcela. */
    val numeroParcela: String? = null,
    /**
     * Valor LIQUIDO esperado da parcela (o SoftExpert manda o liquido). Comparado com o liquido da
     * parcela no SAP: PCH6.InsTotal menos a retencao de categoria Pagamento da parcela.
     */
    val valorParcela: BigDecimal? = null,
    /**
     * Chave de acesso (OPCH.U_ChaveAcesso, campo do TaxPlus): 44 digitos NFe/CTe, 50 digitos NFS-e
     * nacional (sem o prefixo "NFS") ou id `DPS` + 42. Opcional; quando vem, e o primeiro criterio e
     * precisa ser coerente com numero, serie, tipo e fornecedor (senao DADOS_NOTA_DIVERGENTES).
     */
    val chaveAcesso: String? = null,
    /** Tipo do documento: NFE, CTE ou NFSE (sem caixa). Define o filtro por modelo (OPCH.Model). */
    val tipoNota: String? = null,
)

/**
 * Resultado da conferencia. Somente [CodigoConsultaPagamento.PAGO] significa "parcela quitada por
 * pagamento efetuado"; todos os demais codigos pedem tratamento manual.
 */
data class ConsultaPagamentoParcelaResponse(
    val codigo: CodigoConsultaPagamento,
    val mensagem: String,
    val nota: NotaEntradaResumo? = null,
    val parcela: ParcelaResumo? = null,
    val pagamentos: List<PagamentoResumo> = emptyList(),
)

enum class CodigoConsultaPagamento {
    /**
     * Pagamentos efetuados nao cancelados da parcela (liquido de retencao) cobrem o valor liquido,
     * e a baixa no SAP (PaidToDate) tambem. Pagar a mais (juros, multa) continua PAGO.
     */
    PAGO,
    /** Ha valor pago/baixado, mas menor que o liquido da parcela. */
    PAGO_PARCIAL,
    /** Nenhum valor pago nem baixado. */
    EM_ABERTO,
    /**
     * Parcela baixada no SAP sem pagamento efetuado que cubra o liquido: baixa por devolucao,
     * reconciliacao, desconto ou lancamento manual. Retencao na fonte nao cai aqui (a comparacao e
     * pelo liquido). Nao confirmar automaticamente.
     */
    BAIXADA_SEM_PAGAMENTO,
    NOTA_NAO_ENCONTRADA,
    FORNECEDOR_NAO_ENCONTRADO,
    FILIAL_NAO_ENCONTRADA,
    PARCELA_NAO_ENCONTRADA,
    VALOR_DIVERGENTE,
    /**
     * Mais de uma nota atende aos criterios, ou a unica candidata nao pode ser confirmada (NFS-e
     * casada so pelos 9 ultimos digitos, sem chave de acesso no SAP).
     */
    AMBIGUO,
    /**
     * Dados da nota no pedido sao incoerentes entre si ou com o SAP: chave de acesso em formato
     * invalido, ou cujo numero, serie, modelo ou CNPJ do emitente diverge do pedido, ou chave
     * encontrada no SAP em nota de outro numero/modelo. Vai para conferencia manual.
     */
    DADOS_NOTA_DIVERGENTES,
    /**
     * Nota em moeda diferente da moeda local da empresa, ou moeda desconhecida. O SoftExpert nao
     * informa moeda e os valores da PCH6/VPM2 comparados sao os da moeda local: nao da para
     * confirmar com seguranca. Vai para conferencia manual.
     */
    MOEDA_NAO_SUPORTADA,
    /**
     * Nota com retencao de categoria Pagamento cuja divisao por parcela nao fecha (soma de
     * PCH6.WTSum diferente do total de PCH5 categoria P, ou valor negativo): nao da para saber o
     * liquido da parcela com seguranca. Vai para conferencia manual.
     */
    RETENCAO_NAO_SUPORTADA,
}

data class NotaEntradaResumo(
    val docEntry: Int,
    val docNum: Int?,
    val numero: Int?,
    val serie: String?,
    val dataDocumento: String?,
    val cardCode: String?,
    val filial: Int?,
    /** OPCH.DocCur. Preenchida depois de ler as parcelas; null antes disso. */
    val moeda: String? = null,
    /** OPCH.Model (ONFM.AbsEntry), ex.: 39 NFe, 45 CTe, 46/60 NFS-e. */
    val modelo: String? = null,
    /** Como a nota foi encontrada: [CriterioNota]. */
    val criterio: CriterioNota? = null,
)

enum class CriterioNota {
    /** OPCH.U_ChaveAcesso igual a chave do pedido. */
    CHAVE_ACESSO,
    /** OPCH.Serial igual ao numero (com serie/modelo quando informados). */
    NUMERO,
    /** NFS-e com mais de 9 digitos: OPCH.Serial igual aos 9 ultimos, confirmada pela chave da nota no SAP. */
    NUMERO_TRUNCADO,
}

data class ParcelaResumo(
    val numero: Int,
    val vencimento: String?,
    /** PCH6.InsTotal (ja liquido da retencao de categoria Fatura). */
    val valor: BigDecimal,
    /** PCH6.PaidToDate: tudo o que baixou a parcela, inclusive o que nao e pagamento. */
    val valorBaixado: BigDecimal,
    /** Liquido ainda nao pago: [valorLiquido] - [valorPagamentos], nunca negativo. */
    val saldo: BigDecimal,
    /** PCH6.Status: O (aberta) ou C (fechada). */
    val status: String?,
    /**
     * Soma do que os pagamentos efetuados (OVPM nao cancelado) desta parcela pagaram ao fornecedor:
     * VPM2.SumApplied - VPM2.WtAppld (sem a retencao descontada no pagamento).
     */
    val valorPagamentos: BigDecimal,
    /** Retencao de categoria Pagamento desta parcela (PCH6.WTSum validado contra PCH5); 0 sem ela. */
    val retencaoPagamento: BigDecimal = BigDecimal.ZERO,
    /** Valor liquido da parcela no SAP: [valor] - [retencaoPagamento]. Base da comparacao. */
    val valorLiquido: BigDecimal = valor,
)

data class PagamentoResumo(
    val docEntry: Int?,
    val docNum: Int?,
    val data: String?,
    /** VPM2.SumApplied: o que o pagamento baixou da parcela. */
    val valorAplicado: BigDecimal,
    /** VPM2.WtAppld: retencao descontada neste pagamento (ja dentro de [valorAplicado]). */
    val valorRetido: BigDecimal = BigDecimal.ZERO,
    /** [valorAplicado] - [valorRetido]: o que foi pago ao fornecedor. */
    val valorPago: BigDecimal = valorAplicado - valorRetido,
)

/**
 * Linha de `odbc/contas-pagar-notas-numero.sql` / `contas-pagar-notas-chave.sql` (OPCH), via
 * sap-odbc.
 */
data class NotaEntradaSap(
    val docEntry: Int? = null,
    val docNum: Int? = null,
    /** OPCH.Serial: numero da nota. */
    val sequenceSerial: Int? = null,
    /** OPCH.SeriesStr: serie fiscal (ate 3 caracteres, muitas vezes vazia em NFS-e). */
    val seriesString: String? = null,
    val cardCode: String? = null,
    /** OPCH.BPLId. */
    val filial: Int? = null,
    val docDate: String? = null,
    /** OPCH.CANCELED: 'N' normal, 'Y' cancelada, 'C' documento de cancelamento. */
    val cancelado: String? = "N",
    /** OPCH.Model (ONFM.AbsEntry, texto). */
    val modelo: String? = null,
    /** OPCH.U_ChaveAcesso. */
    val chaveAcesso: String? = null,
) {
    companion object {
        fun from(linha: Map<String, Any?>) = NotaEntradaSap(
            docEntry = linha.inteiro("DocEntry"),
            docNum = linha.inteiro("DocNum"),
            sequenceSerial = linha.inteiro("Serial"),
            seriesString = linha.texto("SeriesStr"),
            cardCode = linha.texto("CardCode"),
            filial = linha.inteiro("BPLId"),
            docDate = linha.texto("DocDate"),
            cancelado = linha.texto("CANCELED"),
            modelo = linha.texto("Model"),
            chaveAcesso = linha.texto("U_ChaveAcesso"),
        )
    }
}

/**
 * Linha de `odbc/contas-pagar-parcelas.sql` (PCH6 + OPCH.DocCur + OADM.MainCurncy), via sap-odbc.
 * Valores em moeda local.
 */
data class ParcelaNotaEntradaSap(
    val instlmntId: Int? = null,
    val dueDate: String? = null,
    val insTotal: BigDecimal? = null,
    val paidToDate: BigDecimal? = null,
    val status: String? = null,
    /** OPCH.DocCur: moeda do documento. */
    val moedaDocumento: String? = null,
    /** OADM.MainCurncy: moeda local da empresa. */
    val moedaLocal: String? = null,
    /** PCH6.WTSum: retencao atribuida a parcela. So vale com retencao de categoria Pagamento. */
    val wtSum: BigDecimal? = null,
    /** Soma de PCH5.WTAmnt com Category = 'P' do documento (null = sem retencao no pagamento). */
    val retencaoPagamentoDocumento: BigDecimal? = null,
) {
    companion object {
        fun from(linha: Map<String, Any?>) = ParcelaNotaEntradaSap(
            instlmntId = linha.inteiro("InstlmntID"),
            dueDate = linha.texto("DueDate"),
            insTotal = linha.decimal("InsTotal"),
            paidToDate = linha.decimal("PaidToDate"),
            status = linha.texto("Status"),
            moedaDocumento = linha.texto("DocCur"),
            moedaLocal = linha.texto("MainCurncy"),
            wtSum = linha.decimal("WTSum"),
            retencaoPagamentoDocumento = linha.decimal("RetencaoPagamento"),
        )
    }
}

/** Linha de `odbc/contas-pagar-pagamentos.sql` (VPM2 + OVPM nao cancelado, InvType 18), via sap-odbc. */
data class PagamentoNotaEntradaSap(
    val docEntry: Int? = null,
    val docNum: Int? = null,
    val docDate: String? = null,
    val instId: Int? = null,
    /** VPM2.SumApplied, moeda local. */
    val sumApplied: BigDecimal? = null,
    /** VPM2.WtAppld: retencao (categoria Pagamento) descontada neste pagamento, moeda local. */
    val wtAppld: BigDecimal? = null,
) {
    companion object {
        fun from(linha: Map<String, Any?>) = PagamentoNotaEntradaSap(
            docEntry = linha.inteiro("DocEntry"),
            docNum = linha.inteiro("DocNum"),
            docDate = linha.texto("DocDate"),
            instId = linha.inteiro("InstId"),
            sumApplied = linha.decimal("SumApplied"),
            wtAppld = linha.decimal("WtAppld"),
        )
    }
}

/*
 * Leitura das linhas do sap-odbc: decimal chega como texto (sem perder precisao) e inteiro como
 * numero JSON. Valor ilegivel vira null, e null nunca conta como pago (a regra falha fechada).
 */
private fun Map<String, Any?>.texto(campo: String): String? = this[campo]?.toString()?.trim()?.ifEmpty { null }

private fun Map<String, Any?>.decimal(campo: String): BigDecimal? = texto(campo)?.toBigDecimalOrNull()

private fun Map<String, Any?>.inteiro(campo: String): Int? =
    decimal(campo)?.let { runCatching { it.intValueExact() }.getOrNull() }
