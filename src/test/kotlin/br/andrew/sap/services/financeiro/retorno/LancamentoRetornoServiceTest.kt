package br.andrew.sap.services.financeiro.retorno

import JournalEntry
import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoAutomatico
import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoLog
import br.andrew.sap.model.financeiro.retorno.RetornoBankPlus
import br.andrew.sap.model.financeiro.retorno.RetornoDescontado
import br.andrew.sap.model.financeiro.retorno.TituloDescontado
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.journal.JournalEntriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LancamentoRetornoServiceTest {

    private val journal: JournalEntriesService = mock(JournalEntriesService::class.java)
    private val bankPlus: RetornoBankPlusService = mock(RetornoBankPlusService::class.java)
    private val historico: LancamentoRetornoLogService = mock(LancamentoRetornoLogService::class.java)
    private val automatico: LancamentoRetornoAutomaticoService = mock(LancamentoRetornoAutomaticoService::class.java)
    private val service = LancamentoRetornoService(bankPlus, journal, historico, automatico)

    private val dia = LocalDate.of(2026, 9, 2)

    //o que o Service Layer devolve no POST: o proprio LCM, ja com o numero
    private fun salvoComo(entry: Any, jdtNum: Int) = OData().also {
        it["value"] = it.mapper.writeValueAsString((entry as JournalEntry).also { e -> e.JdtNum = jdtNum })
    }

    private fun titulo(
        nossoNumero: String,
        face: String,
        iof: String,
        juros: String,
        data: LocalDate? = dia,
        divergencia: String? = null,
        situacao: Int? = 15,
    ) = TituloDescontado(nossoNumero, "1592", "CLI0004105", "CLIENTE", data, LocalDate.of(2026, 10, 2),
        BigDecimal(face), BigDecimal(iof), BigDecimal(juros), divergencia, situacao)

    /**
     * Totais do retorno 4745 (22 boletos), que o BankPlus lancou no LCM 788247: parcelas
     * 368.592,05, IOF 4.008,27, juros 19.639,61. Aqui em dois boletos com os mesmos totais.
     */
    private fun retorno4745(
        titulos: List<TituloDescontado> = listOf(
            titulo("35158540000020129", "300000.00", "3000.00", "15000.00"),
            titulo("35158540000020356", "68592.05", "1008.27", "4639.61"),
        ),
        lcm: Int? = null,
        lcmBankPlus: Int? = null,
        processado: Boolean = true,
        mesmoArquivo: Int? = null,
        contaIof: String? = "5.1.1.012.00009",
    ) = RetornoBankPlus(
        retorno = 4745,
        arquivo = "CBR643.C3515854.2026090300015986.021543.RET",
        dataArquivo = LocalDate.of(2026, 9, 8),
        processado = processado,
        contrato = 3,
        convenio = "3515854",
        filial = 2,
        nomeFilial = "SUSTENNUTRI - Matriz",
        contaDesconto = "2.1.3.001.07901",
        contaIof = contaIof,
        contaJuros = "5.1.1.012.00006",
        contaClientes = "1.1.2.001.00999",
        costingCode = "500",
        costingCode2 = "50000203",
        lcm = lcm,
        lcmBankPlus = lcmBankPlus,
        mesmoArquivo = mesmoArquivo,
        titulos = titulos,
    )

    @Test
    fun partidaIgualAoLcmDoBankPlusDoRetorno4745() {
        val previa = service.previa(retorno4745())

        assertTrue(previa.podeLancar, previa.erros.toString())
        assertEquals(
            listOf(
                listOf("2.1.3.001.07901", "368592.05", "0", null, null),
                listOf("5.1.1.012.00009", "4008.27", "0", "500", "50000203"),
                listOf("5.1.1.012.00006", "19639.61", "0", "500", "50000203"),
                listOf("1.1.2.001.00999", "0", "392239.93", null, null),
            ),
            previa.linhas.map { listOf(it.conta, it.debito.toPlainString(), it.credito.toPlainString(), it.costingCode, it.costingCode2) },
        )
        assertEquals(dia, previa.dataLancamento)
        assertEquals("3515854-4745", previa.reference)
        assertEquals("Retorno Bancario 4745 - Duplicata descontada", previa.memo)
    }

    @Test
    fun boletoQueAApiJaLiquidouEntraNoLcm() {
        //e o caso que o LCM do BankPlus deixa de fora
        val pago = titulo("35458620000003421", "1788.49", "10.01", "25.77",
            divergencia = "Boleto com status Liquidado não pode ser processado")
        val previa = service.previa(retorno4745(titulos = listOf(titulo("35158540000020129", "1000.00", "1.00", "2.00"), pago)))

        assertTrue(previa.podeLancar)
        assertTrue(previa.titulos.single { it.nossoNumero == "35458620000003421" }.jaPago)
        assertEquals(BigDecimal("2788.49"), previa.totalFace)
        assertEquals(BigDecimal("2827.27"), previa.linhas.last().credito)
    }

    @Test
    fun naoLancaPorCimaDoLcmDoBankPlus() {
        val previa = service.previa(retorno4745(lcmBankPlus = 1493027))
        assertFalse(previa.podeLancar)
        assertEquals(
            listOf("O BankPlus ja gerou o LCM de desconto deste retorno (LCM 1493027). Lancar de novo duplicaria o desconto."),
            previa.erros,
        )
    }

    @Test
    fun recusaRetornoNaoProcessadoArquivoRepetidoEContaFaltando() {
        val previa = service.previa(retorno4745(processado = false, mesmoArquivo = 4746, contaIof = null))
        assertEquals(
            listOf(
                "O retorno 4745 ainda nao foi processado no BankPlus.",
                "O contrato 3 do BankPlus nao tem conta de IOF.",
                "O arquivo CBR643.C3515854.2026090300015986.021543.RET tambem foi importado no retorno 4746 do BankPlus.",
            ),
            previa.erros,
        )
        assertTrue(previa.linhas.isEmpty())
    }

    @Test
    fun recusaDatasDiferentesEBoletoRepetido() {
        val previa = service.previa(retorno4745(titulos = listOf(
            titulo("1", "100.00", "1.00", "1.00"),
            titulo("1", "100.00", "1.00", "1.00", data = dia.plusDays(1)),
        )))
        assertEquals(
            listOf(
                "Boleto em mais de uma ocorrencia de desconto do retorno: 1.",
                "Os boletos tem datas de ocorrencia diferentes (2026-09-02, 2026-09-03); o LCM e um por retorno e precisa de uma data so.",
            ),
            previa.erros,
        )
        assertNull(previa.dataLancamento)
    }

    @Test
    fun recusaBoletoSemDataMesmoComOsOutrosNaMesmaData() {
        val previa = service.previa(retorno4745(titulos = listOf(
            titulo("1", "100.00", "1.00", "1.00"),
            titulo("2", "100.00", "1.00", "1.00", data = null),
        )))
        assertEquals(listOf("Boleto sem data de ocorrencia: 2."), previa.erros)
    }

    @Test
    fun lancaComReferenciaFilialDataERateioSoNasDespesas() {
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745())
        whenever(journal.save(any())).thenAnswer { salvoComo(it.arguments[0], 900) }

        val resultado = service.lancar(4745, "MANUAL", "FRANCISCO")!!

        assertEquals(900, resultado.jdtNum)
        assertFalse(resultado.jaExistia)
        val entry = argumentCaptor<JournalEntry>().also { verify(journal).save(it.capture()) }.firstValue
        assertEquals("3515854-4745", entry.Reference)
        assertEquals("DDES", entry.TransactionCode)
        assertEquals("2026-09-02", entry.ReferenceDate)
        assertEquals("2026-09-02", entry.taxDate)
        val linhas = entry.journalEntryLines
        assertEquals(listOf(368592.05, 4008.27, 19639.61, 0.0), linhas.map { it.debit })
        assertEquals(392239.93, linhas.last().Credit)
        assertTrue(linhas.all { it.getBPLID() == 2 })
        assertEquals(listOf(null, "500", "500", null), linhas.map { it.costingCode })
    }

    @Test
    fun retornoJaLancadoDevolveOLcmSemGravar() {
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745(lcm = 1500000))
        val resultado = service.lancar(4745, "MANUAL", "FRANCISCO")!!
        assertEquals(1500000, resultado.jdtNum)
        assertTrue(resultado.jaExistia)
        verify(journal, never()).save(any())
    }

    @Test
    fun comErroNaoGravaENaoLancaExcecao() {
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745(lcmBankPlus = 1493027))
        val resultado = service.lancar(4745, "MANUAL", "FRANCISCO")!!
        assertNull(resultado.jdtNum)
        assertFalse(resultado.previa.podeLancar)
        verify(journal, never()).save(any())
    }

    @Test
    fun retornoInexistenteDevolveNull() {
        assertNull(service.previa(1))
        assertNull(service.lancar(1, "MANUAL", "FRANCISCO"))
    }

    @Test
    fun doisLancarDoMesmoRetornoAoMesmoTempoGravamUmSo() {
        //o segundo so entra depois do primeiro gravar, e ai a releitura ja ve o LCM
        var gravado: Int? = null
        whenever(bankPlus.buscar(4745)).thenAnswer { retorno4745(lcm = gravado) }
        whenever(journal.save(any())).thenAnswer {
            Thread.sleep(100)
            gravado = 900
            salvoComo(it.arguments[0], 900)
        }
        val pool = Executors.newFixedThreadPool(2)
        val largada = CountDownLatch(1)
        val resultados = (1..2).map { pool.submit<Boolean> { largada.await(); service.lancar(4745, "MANUAL", "FRANCISCO")!!.jaExistia } }
        largada.countDown()
        val jaExistia = resultados.map { it.get(5, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(listOf(false, true), jaExistia.sorted())
        verify(journal).save(any())
    }

    @Test
    fun boletoSemSituacaoDescontadaAvisaMasNaoImpedeOLancamento() {
        //o BankPlus nao poe na Carteira Descontada (15) o boleto que a API ja tinha liquidado
        val pago = titulo("35158540000019072", "1000.00", "1.00", "2.00", situacao = 3,
            divergencia = "Boleto com status Liquidado não pode ser processado")
        whenever(bankPlus.buscar(4831)).thenReturn(retorno4745(titulos = listOf(titulo("35158540000019669", "500.00", "1.00", "2.00"), pago)))
        whenever(journal.save(any())).thenAnswer { salvoComo(it.arguments[0], 900) }

        val previa = service.previa(4831)!!
        assertTrue(previa.podeLancar)
        val aviso = "1 boleto(s) sem a situacao Carteira Descontada no BankPlus, ajustar a mao: 35158540000019072."
        assertEquals(listOf(aviso), previa.avisos)

        service.lancar(4831, "AUTOMATICO", "AUTOMATICO")
        verify(historico).registrar("AUTOMATICO", "LANCADO", "AUTOMATICO", 4831, 900, BigDecimal("1506.00"), aviso)
    }

    @Test
    fun historicoRegistraLancadoComOValorDoLcm() {
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745())
        whenever(journal.save(any())).thenAnswer { salvoComo(it.arguments[0], 900) }
        service.lancar(4745, "MANUAL", "FRANCISCO")
        verify(historico).registrar("MANUAL", "LANCADO", "FRANCISCO", 4745, 900, BigDecimal("392239.93"))
    }

    @Test
    fun falhaNoSapVaiParaOHistoricoESobeAExcecao() {
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745())
        whenever(journal.save(any())).thenThrow(RuntimeException("Periodo contabil bloqueado"))
        assertThrows<RuntimeException> { service.lancar(4745, "MANUAL", "FRANCISCO") }
        verify(historico).registrar("MANUAL", "ERRO", "FRANCISCO", 4745, mensagem = "Periodo contabil bloqueado")
    }

    private fun automaticoLigadoEm(data: String?, ativo: String = "Y") =
        whenever(automatico.estado()).thenReturn(LancamentoRetornoAutomatico("AUTOMATICO", "AUTOMATICO", ativo, "FRANCISCO", data))

    private fun pendente(retorno: Int, lcm: Int? = null) = RetornoDescontado(retorno, "CBR643.RET", dia, 3, "3515854", 2,
        "Matriz", 2, 0, 0, BigDecimal("368592.05"), BigDecimal("4008.27"), BigDecimal("19639.61"), lcm, null)

    @Test
    fun automaticoDesligadoNaoFazNada() {
        automaticoLigadoEm("2026-09-01T10:00:00", ativo = "N")
        assertEquals(0, service.lancarAutomatico(dia))
        verify(bankPlus, never()).listar(any(), any())
    }

    @Test
    fun automaticoLancaSoOPendenteDesdeODiaEmQueFoiLigado() {
        automaticoLigadoEm("2026-09-01T10:00:00")
        whenever(bankPlus.listar(LocalDate.of(2026, 9, 1), dia)).thenReturn(listOf(pendente(4745), pendente(4744, lcm = 800)))
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745())
        whenever(journal.save(any())).thenAnswer { salvoComo(it.arguments[0], 900) }

        assertEquals(1, service.lancarAutomatico(dia))

        verify(bankPlus, never()).buscar(4744)
        verify(historico).registrar("AUTOMATICO", "LANCADO", "AUTOMATICO", 4745, 900, BigDecimal("392239.93"))
    }

    @Test
    fun automaticoNaoRepeteAMesmaRecusaNoHistorico() {
        automaticoLigadoEm("2026-09-01T10:00:00")
        whenever(bankPlus.listar(any(), any())).thenReturn(listOf(pendente(4745)))
        whenever(bankPlus.buscar(4745)).thenReturn(retorno4745(lcmBankPlus = 1493027))
        val motivo = "O BankPlus ja gerou o LCM de desconto deste retorno (LCM 1493027). Lancar de novo duplicaria o desconto."

        //primeira vez: registra; depois a ultima recusa ja tem o mesmo motivo
        service.lancarAutomatico(dia)
        whenever(historico.ultimaRecusa(4745)).thenReturn(LancamentoRetornoLog(U_Resultado = "RECUSADO", U_Mensagem = motivo))
        service.lancarAutomatico(dia)

        verify(historico).registrar("AUTOMATICO", "RECUSADO", "AUTOMATICO", 4745, mensagem = motivo)
        verify(journal, never()).save(any())
    }
}
