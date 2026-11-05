package br.andrew.sap.model.financeiro.retorno

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate

/*
 * Datas com @JsonFormat explicito: o ObjectMapper da aplicacao serializa LocalDate como array
 * ([2026,9,16]), e a tela espera "2026-09-16".
 */

/** Um retorno do BankPlus com ocorrencia de desconto, como aparece na lista da tela. */
class RetornoDescontado(
    val retorno: Int,
    val arquivo: String,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val dataArquivo: LocalDate?,
    val contrato: Int,
    val convenio: String,
    val filial: Int?,
    val nomeFilial: String?,
    val boletos: Int,
    val jaPagos: Int,
    //boletos fora da situacao de descontada no BankPlus (ver TituloDescontado.semSituacaoDescontada)
    val semSituacao: Int,
    val totalFace: BigDecimal,
    val totalIof: BigDecimal,
    val totalJuros: BigDecimal,
    val lcm: Int?,
    val lcmBankPlus: Int?,
)

/**
 * Retorno lido do BankPlus com tudo o que o LCM precisa: contas do contrato, rateio do retorno,
 * filial do contrato e os boletos da ocorrencia 72 (modo Descontado).
 */
class RetornoBankPlus(
    val retorno: Int,
    val arquivo: String,
    val dataArquivo: LocalDate?,
    val processado: Boolean,
    val contrato: Int,
    val convenio: String,
    val filial: Int?,
    val nomeFilial: String?,
    val contaDesconto: String?,
    val contaIof: String?,
    val contaJuros: String?,
    val contaClientes: String?,
    val costingCode: String?,
    val costingCode2: String?,
    val lcm: Int?,
    val lcmBankPlus: Int?,
    val mesmoArquivo: Int?,
    val titulos: List<TituloDescontado>,
)

class TituloDescontado(
    val nossoNumero: String,
    val documento: String?,
    val cliente: String?,
    val nomeCliente: String?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val dataOcorrencia: LocalDate?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val vencimento: LocalDate?,
    val valorFace: BigDecimal,
    val iof: BigDecimal,
    val juros: BigDecimal,
    //texto do BankPlus quando ele nao processou a ocorrencia (ex.: boleto ja liquidado pela API)
    val divergencia: String?,
    //situacao do boleto no BankPlus (IV_IB_BillOfExchange.Situation)
    val situacao: Int?,
) {
    //o caso que o LCM do BankPlus deixa de fora e o nosso inclui
    val jaPago: Boolean
        get() = divergencia?.contains("Liquidado") == true

    //o BankPlus nao muda a situacao do boleto que a API ja liquidou: fica fora da Carteira
    //Descontada (15) ou Descontada - Assets (19) e precisa de ajuste manual
    val semSituacaoDescontada: Boolean
        get() = situacao !in SITUACOES_DESCONTADA

    companion object {
        val SITUACOES_DESCONTADA = setOf(15, 19)
    }
}

/**
 * O que a tela mostra antes de lancar: os boletos, os totais e o LCM que sera gerado. Com `erros`
 * preenchido ou `lcmExistente` informado, nao ha o que lancar.
 */
class PreviaLancamentoRetorno(
    val retorno: Int,
    val arquivo: String,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val dataArquivo: LocalDate?,
    val contrato: Int,
    val convenio: String,
    val filial: Int?,
    val nomeFilial: String?,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val dataLancamento: LocalDate?,
    val reference: String,
    val memo: String,
    val titulos: List<TituloDescontado>,
    val totalFace: BigDecimal,
    val totalIof: BigDecimal,
    val totalJuros: BigDecimal,
    val linhas: List<LinhaLcmRetorno>,
    val lcmExistente: Int?,
    val lcmBankPlus: Int?,
    val erros: List<String>,
    //nao impedem o lancamento, so pedem atencao
    val avisos: List<String> = emptyList(),
) {
    val podeLancar: Boolean
        get() = erros.isEmpty() && lcmExistente == null
}

class LinhaLcmRetorno(
    val descricao: String,
    val conta: String,
    val debito: BigDecimal,
    val credito: BigDecimal,
    val costingCode: String? = null,
    val costingCode2: String? = null,
)

class ResultadoLancamentoRetorno(
    val jdtNum: Int?,
    val jaExistia: Boolean,
    val previa: PreviaLancamentoRetorno,
)
