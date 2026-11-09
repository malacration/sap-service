package br.andrew.sap.services.financeiro.retorno

import br.andrew.sap.model.financeiro.retorno.RetornoBankPlus
import br.andrew.sap.model.financeiro.retorno.RetornoDescontado
import br.andrew.sap.model.financeiro.retorno.TituloDescontado
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Retornos de cobranca que o BankPlus importou (IV_IB_ReturnOfBank / IV_IB_ReturnBankOcurrence),
 * so a ocorrencia de desconto do BB (72, modo Descontado). O BankPlus grava ali o IOF e os juros
 * de cada boleto, inclusive dos que ele nao processa porque a API ja liquidou.
 *
 * Tabelas nativas do BankPlus, fora do alcance do SQLQueries do Service Layer, por isso a leitura
 * passa pelo sap-odbc.
 */
@Service
class RetornoBankPlusService(val odbc: OdbcClient) {

    //o validador do sap-odbc recusa instrucao com comentario; SqlResource ja tira
    private val sqlRetornos by lazy { SqlResource.carregar("odbc/lancamento-retorno-lista.sql") }
    private val sqlRetorno by lazy { SqlResource.carregar("odbc/lancamento-retorno-detalhe.sql") }

    fun listar(de: LocalDate, ate: LocalDate): List<RetornoDescontado> =
        odbc.consultar(sqlRetornos, mapOf("de" to de.toString(), "ate" to ate.toString()), maxRows = 1000).rows.map {
            RetornoDescontado(
                retorno = int(it["Retorno"])!!,
                arquivo = it["Arquivo"]?.toString() ?: "",
                dataArquivo = data(it["DataArquivo"]),
                contrato = int(it["Contrato"])!!,
                convenio = it["Convenio"]?.toString() ?: "",
                filial = int(it["Filial"]),
                nomeFilial = it["NomeFilial"]?.toString(),
                boletos = int(it["Boletos"]) ?: 0,
                jaPagos = int(it["JaPagos"]) ?: 0,
                semSituacao = int(it["SemSituacao"]) ?: 0,
                totalFace = valor(it["Face"]),
                totalIof = valor(it["Iof"]),
                totalJuros = valor(it["Juros"]),
                lcm = int(it["Lcm"]),
                lcmBankPlus = int(it["LcmBankPlus"]),
            )
        }

    /** null quando o retorno nao existe ou nao tem ocorrencia de desconto. */
    fun buscar(retorno: Int): RetornoBankPlus? {
        val linhas = odbc.consultar(sqlRetorno, mapOf("retorno" to retorno), maxRows = 5000).rows
        val r = linhas.firstOrNull() ?: return null
        return RetornoBankPlus(
            retorno = int(r["Retorno"])!!,
            arquivo = r["Arquivo"]?.toString() ?: "",
            dataArquivo = data(r["DataArquivo"]),
            //3 = Processado (IV_IB_ReturnBankStatus)
            processado = int(r["StatusRetorno"]) == 3,
            contrato = int(r["Contrato"])!!,
            convenio = r["Convenio"]?.toString() ?: "",
            filial = int(r["Filial"]),
            nomeFilial = r["NomeFilial"]?.toString(),
            contaDesconto = texto(r["ContaDesconto"]),
            contaIof = texto(r["ContaIof"]),
            contaJuros = texto(r["ContaJuros"]),
            contaClientes = texto(r["ContaClientes"]),
            costingCode = texto(r["Rateio"]),
            costingCode2 = texto(r["Rateio2"]),
            lcm = int(r["Lcm"]),
            lcmBankPlus = int(r["LcmBankPlus"]),
            mesmoArquivo = int(r["MesmoArquivo"]),
            titulos = linhas.map {
                TituloDescontado(
                    nossoNumero = it["NossoNumero"]?.toString() ?: "",
                    documento = texto(it["Documento"]),
                    cliente = texto(it["Cliente"]),
                    nomeCliente = texto(it["NomeCliente"]),
                    dataOcorrencia = data(it["DataOcorrencia"]),
                    vencimento = data(it["Vencimento"]),
                    valorFace = valor(it["Face"]),
                    iof = valor(it["Iof"]),
                    juros = valor(it["Juros"]),
                    divergencia = texto(it["Divergencia"]),
                    situacao = int(it["Situacao"]),
                )
            },
        )
    }

    //o sap-odbc devolve inteiro como Integer ou Long, decimal como texto ("4008.27") e data como
    //"2026-09-02T00:00"
    private fun int(v: Any?): Int? = (v as Number?)?.toInt()
    private fun valor(v: Any?): BigDecimal = v?.toString()?.let { BigDecimal(it) } ?: BigDecimal.ZERO
    private fun data(v: Any?): LocalDate? = v?.toString()?.take(10)?.let { LocalDate.parse(it) }
    private fun texto(v: Any?): String? = v?.toString()?.trim()?.takeIf { it.isNotEmpty() }
}
