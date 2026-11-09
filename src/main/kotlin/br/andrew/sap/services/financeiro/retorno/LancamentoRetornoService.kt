package br.andrew.sap.services.financeiro.retorno

import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.financeiro.retorno.LinhaLcmRetorno
import br.andrew.sap.model.financeiro.retorno.PreviaLancamentoRetorno
import br.andrew.sap.model.financeiro.retorno.ResultadoLancamentoRetorno
import br.andrew.sap.model.financeiro.retorno.RetornoBankPlus
import br.andrew.sap.model.financeiro.retorno.RetornoDescontado
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.journal.JournalEntriesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * LCM do desconto de duplicatas a partir dos retornos importados no BankPlus.
 *
 * O BankPlus gera esse LCM sozinho, mas deixa de fora os boletos que a API ja liquidou antes de o
 * retorno chegar ("Boleto com status Liquidado nao pode ser processado"), e o LCM nao bate com o
 * desconto do dia. Aqui o LCM sai com todos os boletos da ocorrencia 72, na mesma partida do
 * BankPlus (LCM 788247, retorno 4745), com as contas do contrato e o rateio do retorno:
 *
 *   D Duplicatas descontadas   valor dos titulos informado pelo banco
 *   D IOF                      IOF    (com rateio)
 *   D Juros de antecipacao     juros  (com rateio)
 *   C Clientes                 titulos + IOF + juros
 *
 * Idempotente por Reference (convenio-retorno) + TransactionCode DDES. Todo lancamento, pela tela
 * ou pela rotina automatica, fica no historico (LANC_RET_LOG).
 */
@Service
class LancamentoRetornoService(
    val retornos: RetornoBankPlusService,
    val journalEntriesService: JournalEntriesService,
    val historico: LancamentoRetornoLogService,
    val automatico: LancamentoRetornoAutomaticoService,
) {
    private val log = LoggerFactory.getLogger(LancamentoRetornoService::class.java)

    //trava por retorno: dois lancar do mesmo retorno ao mesmo tempo passariam os dois pela busca de
    //LCM existente antes de qualquer um gravar. Vale so dentro desta instancia; com mais de uma
    //replica a garantia teria de vir do SAP (TransactionNotification).
    private val travas = Array(64) { Any() }

    fun listar(de: LocalDate, ate: LocalDate): List<RetornoDescontado> = retornos.listar(de, ate)

    fun previa(retorno: Int): PreviaLancamentoRetorno? = retornos.buscar(retorno)?.let { previa(it) }

    fun previa(retorno: RetornoBankPlus): PreviaLancamentoRetorno {
        val erros = mutableListOf<String>()
        val titulos = retorno.titulos

        if (!retorno.processado)
            erros += "O retorno ${retorno.retorno} ainda nao foi processado no BankPlus."
        if (retorno.filial == null)
            erros += "O contrato ${retorno.contrato} do BankPlus nao tem filial (CompanyID)."
        listOf(
            "Duplicatas Descontadas" to retorno.contaDesconto,
            "IOF" to retorno.contaIof,
            "Juros" to retorno.contaJuros,
            "Contabil (clientes)" to retorno.contaClientes,
        ).filter { it.second == null }.takeIf { it.isNotEmpty() }?.let { faltando ->
            erros += "O contrato ${retorno.contrato} do BankPlus nao tem conta de ${faltando.joinToString(", ") { it.first }}."
        }
        //com o contrato ainda contabilizando o desconto, lancar aqui duplicaria o LCM
        retorno.lcmBankPlus?.let {
            erros += "O BankPlus ja gerou o LCM de desconto deste retorno (LCM $it). Lancar de novo duplicaria o desconto."
        }
        retorno.mesmoArquivo?.let {
            erros += "O arquivo ${retorno.arquivo} tambem foi importado no retorno $it do BankPlus."
        }

        //o mesmo boleto duas vezes somaria o desconto duas vezes
        titulos.groupBy { it.nossoNumero }.filterValues { it.size > 1 }.keys.takeIf { it.isNotEmpty() }?.let {
            erros += "Boleto em mais de uma ocorrencia de desconto do retorno: ${it.joinToString(", ")}."
        }
        titulos.filter { it.juros + it.iof >= it.valorFace }.forEach {
            erros += "Boleto ${it.nossoNumero}: juros + IOF (${it.juros + it.iof}) nao e menor que o valor da parcela (${it.valorFace})."
        }

        val datas = titulos.mapNotNull { it.dataOcorrencia }.distinct().sorted()
        if (datas.size > 1)
            erros += "Os boletos tem datas de ocorrencia diferentes (${datas.joinToString(", ")}); " +
                "o LCM e um por retorno e precisa de uma data so."
        //boleto sem data nao pode ir na data dos outros sem conferencia
        titulos.filter { it.dataOcorrencia == null }.takeIf { it.isNotEmpty() }?.let {
            erros += "Boleto sem data de ocorrencia: ${it.joinToString(", ") { t -> t.nossoNumero }}."
        }
        val dataLancamento = datas.singleOrNull()

        val avisos = mutableListOf<String>()
        titulos.filter { it.semSituacaoDescontada }.takeIf { retorno.processado && it.isNotEmpty() }?.let {
            avisos += "${it.size} boleto(s) sem a situacao Carteira Descontada no BankPlus, ajustar a mao: " +
                it.joinToString(", ") { t -> t.nossoNumero } + "."
        }

        val totalFace = titulos.sumOf { it.valorFace }
        val totalIof = titulos.sumOf { it.iof }
        val totalJuros = titulos.sumOf { it.juros }

        return PreviaLancamentoRetorno(
            retorno = retorno.retorno,
            arquivo = retorno.arquivo,
            dataArquivo = retorno.dataArquivo,
            contrato = retorno.contrato,
            convenio = retorno.convenio,
            filial = retorno.filial,
            nomeFilial = retorno.nomeFilial,
            dataLancamento = dataLancamento,
            reference = reference(retorno),
            //Memo do OJDT tem 50 posicoes; o BankPlus usa "Retorno Bancario <retorno>"
            memo = "Retorno Bancario ${retorno.retorno} - Duplicata descontada".take(50),
            titulos = titulos,
            totalFace = totalFace,
            totalIof = totalIof,
            totalJuros = totalJuros,
            linhas = linhas(retorno, totalFace, totalIof, totalJuros),
            lcmExistente = retorno.lcm,
            lcmBankPlus = retorno.lcmBankPlus,
            erros = erros,
            avisos = avisos,
        )
    }

    /**
     * null quando o retorno nao existe. Com erro na previa nao lanca e devolve a previa com os
     * erros, sem excecao: e recusa de regra, nao falha do servidor (excecao viraria HTTP 500 e
     * alerta no Telegram).
     */
    fun lancar(retorno: Int, origem: String, usuario: String): ResultadoLancamentoRetorno? {
        val resultado = try {
            synchronized(travas[Math.floorMod(retorno, travas.size)]) { lancarSemConcorrencia(retorno) }
        } catch (e: Exception) {
            historico.registrar(origem, "ERRO", usuario, retorno, mensagem = e.message)
            throw e
        } ?: return null
        val p = resultado.previa
        when {
            resultado.jdtNum == null ->
                historico.registrar(origem, "RECUSADO", usuario, retorno, mensagem = p.erros.joinToString(" "))
            resultado.jaExistia ->
                historico.registrar(origem, "JA_EXISTIA", usuario, retorno, resultado.jdtNum)
            else ->
                historico.registrar(origem, "LANCADO", usuario, retorno, resultado.jdtNum, p.totalFace + p.totalIof + p.totalJuros,
                    p.avisos.joinToString(" ").ifEmpty { null })
        }
        return resultado
    }

    /**
     * Lanca sozinho os retornos pendentes, se o automatico estiver ligado. So retornos com arquivo a
     * partir do dia em que foi ligado (e no maximo JANELA_DIAS para tras): o pendente antigo pode
     * ter sido resolvido a mao e fica para a tela. Recusa repetida (mesmo retorno, mesmo motivo) nao
     * vai de novo para o historico a cada execucao.
     *
     * @return quantos LCMs foram gerados
     */
    fun lancarAutomatico(hoje: LocalDate = LocalDate.now()): Int {
        val estado = automatico.estado()
        if (estado.U_Ativo != "Y") return 0
        val ligadoEm = estado.U_Data?.take(10)?.let { LocalDate.parse(it) } ?: return 0

        var lancados = 0
        retornos.listar(maxOf(ligadoEm, hoje.minusDays(JANELA_DIAS)), hoje)
            .filter { it.lcm == null }
            .forEach { r ->
                val previa = previa(r.retorno) ?: return@forEach
                if (previa.erros.isNotEmpty()) {
                    val motivo = previa.erros.joinToString(" ")
                    if (historico.ultimaRecusa(r.retorno)?.U_Mensagem != motivo.take(254))
                        historico.registrar(AUTOMATICO, "RECUSADO", AUTOMATICO, r.retorno, mensagem = motivo)
                    return@forEach
                }
                try {
                    if (lancar(r.retorno, AUTOMATICO, AUTOMATICO)?.let { it.jdtNum != null && !it.jaExistia } == true)
                        lancados++
                } catch (e: Exception) {
                    //ja foi para o historico; segue para o proximo retorno
                    log.error("Falha no lancamento automatico do retorno ${r.retorno}", e)
                }
            }
        return lancados
    }

    private fun lancarSemConcorrencia(id: Int): ResultadoLancamentoRetorno? {
        val previa = previa(id) ?: return null
        if (previa.erros.isNotEmpty())
            return ResultadoLancamentoRetorno(null, false, previa)
        previa.lcmExistente?.let { return ResultadoLancamentoRetorno(it, true, previa) }

        val data = previa.dataLancamento!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val entry = JournalEntry(
            previa.linhas.map {
                JournalEntryLines(it.conta, it.debito.toDouble(), it.credito.toDouble(), previa.filial!!, it.costingCode, it.costingCode2)
            },
            previa.memo,
        ).also {
            it.taxDate = data
            it.ReferenceDate = data
            it.TransactionCode = TransactionCodeTypes.DDES.name
            it.Reference = previa.reference
            it.Reference2 = previa.arquivo.take(100)
        }

        //save direto, sem saveOrRecouverReference: o estorno copia Ref1 e TransCode, e a busca dele
        //devolveria o LCM estornado. A releitura acima, sob a trava, ja conferiu o LCM ativo.
        val salvo = journalEntriesService.save(entry).tryGetValue<JournalEntry>()
        log.info("Lancamento de Retorno: LCM ${salvo.JdtNum} gerado para o retorno ${previa.retorno} (${previa.arquivo})")
        return ResultadoLancamentoRetorno(salvo.JdtNum, false, previa)
    }

    companion object {
        const val AUTOMATICO = "AUTOMATICO"
        const val JANELA_DIAS = 30L
    }

    //chave de idempotencia, gravada no Ref1 do LCM; a mesma que os SQLs procuram
    private fun reference(retorno: RetornoBankPlus) = "${retorno.convenio}-${retorno.retorno}"

    private fun linhas(retorno: RetornoBankPlus, face: BigDecimal, iof: BigDecimal, juros: BigDecimal): List<LinhaLcmRetorno> {
        if (listOf(retorno.contaDesconto, retorno.contaIof, retorno.contaJuros, retorno.contaClientes).any { it == null })
            return listOf()
        return listOf(
            LinhaLcmRetorno("Duplicatas descontadas", retorno.contaDesconto!!, face, BigDecimal.ZERO),
            LinhaLcmRetorno("IOF", retorno.contaIof!!, iof, BigDecimal.ZERO, retorno.costingCode, retorno.costingCode2),
            LinhaLcmRetorno("Juros de antecipacao", retorno.contaJuros!!, juros, BigDecimal.ZERO, retorno.costingCode, retorno.costingCode2),
            LinhaLcmRetorno("Clientes", retorno.contaClientes!!, BigDecimal.ZERO, face + iof + juros),
        ).filter { it.debito.signum() != 0 || it.credito.signum() != 0 }
    }
}
