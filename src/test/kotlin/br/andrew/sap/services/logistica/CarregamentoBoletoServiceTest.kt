package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.BoletoIdsConfig
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.fiscal.OrigemBoletoEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.springframework.web.client.ResourceAccessException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CarregamentoBoletoServiceTest {

    private val gerados = mutableListOf<String>()

    private fun boleto(parcela: Int, status: String = "Confirmado") = Boleto().also {
        it.id = parcela * 100; it.numeroDaParcela = parcela; it.statusDescricao = status
    }

    private fun nota(docEntry: Int, forma: String? = "BB-RC-BOL-1199", parcelas: Int = 1) =
        Document("C0001", null, listOf(), "2").also { d ->
            d.docEntry = docEntry
            d.SequenceSerial = "${docEntry + 5000}"
            d.paymentMethod = forma
            d.documentInstallments = (1..parcelas).map { n -> Installment(null, 10.0).also { it.InstallmentId = n } }
        }

    // boletos por DocEntry; geraFalha = "doc=X parcela=Y" que a BankPlus recusa
    private fun bankPlus(boletos: Map<Int, List<Boleto>> = mapOf(), listaFalha: Set<Int> = setOf(),
                         geraFalha: Set<String> = setOf(), aoListar: () -> Unit = {}) =
        mock(BankPlusService::class.java, Answer { inv ->
            when (inv.method.name) {
                "getBoletosBy" -> {
                    if (inv.arguments.size != 4) return@Answer null
                    assertEquals("2", inv.arguments[0])
                    assertEquals(OrigemBoletoEnum.notafiscal, inv.arguments[2])
                    assertEquals(true, inv.arguments[3], "listagem sempre com prazo")
                    aoListar()
                    val doc = (inv.arguments[1] as String).toInt()
                    if (doc in listaFalha) throw ResourceAccessException("Read timed out")
                    boletos[doc] ?: listOf<Boleto>()
                }
                "geraBoletos" -> {
                    assertEquals(2, inv.arguments[0])
                    assertEquals(OrigemBoletoEnum.notafiscal, inv.arguments[3])
                    val chave = "doc=${inv.arguments[1]} parcela=${inv.arguments[2]}"
                    synchronized(gerados) { gerados.add(chave) }
                    if (chave in geraFalha) throw Exception("BankPlus recusou")
                    ByteArray(0)
                }
                else -> null
            }
        })

    private fun servico(bank: BankPlusService = bankPlus()) =
        CarregamentoBoletoService(bank, BoletoIdsConfig(listOf("BB-RC-BOL-1199", "BB-RC-BL-126455")))

    @Test
    fun `nota com forma fora de boleto ids e pulada`() {
        val r = servico().gerar(10, listOf(nota(1, forma = "PIX-RC")))
        assertEquals(SituacaoBoleto.NAO_E_BOLETO, r.single().situacao)
        assertTrue(r.single().mensagem.contains("PIX-RC"), r.single().mensagem)
        assertEquals(listOf<String>(), gerados)
    }

    @Test
    fun `parcela com boleto ativo nao gera de novo`() {
        val r = servico(bankPlus(mapOf(1 to listOf(boleto(1))))).gerar(10, listOf(nota(1, parcelas = 2)))
        assertEquals(listOf("doc=1 parcela=2"), gerados)
        assertEquals(SituacaoBoleto.GERADO, r.single().situacao)
        assertEquals("NF 5001", r.single().nota)
    }

    @Test
    fun `boleto cancelado conta como sem boleto`() {
        servico(bankPlus(mapOf(1 to listOf(boleto(1, "Cancelado")), 2 to listOf(boleto(1, "Aguardando Cancelamento")))))
            .gerar(10, listOf(nota(1), nota(2)))
        assertEquals(listOf("doc=1 parcela=1", "doc=2 parcela=1"), gerados)
    }

    @Test
    fun `todas as parcelas ativas`() {
        val r = servico(bankPlus(mapOf(1 to listOf(boleto(1), boleto(2))))).gerar(10, listOf(nota(1, parcelas = 2)))
        assertEquals(SituacaoBoleto.JA_TINHA, r.single().situacao)
        assertEquals(listOf<String>(), gerados)
    }

    @Test
    fun `falha na parcela 2 nao desfaz as outras`() {
        val r = servico(bankPlus(geraFalha = setOf("doc=1 parcela=2"))).gerar(10, listOf(nota(1, parcelas = 3)))
        assertEquals(listOf("doc=1 parcela=1", "doc=1 parcela=2", "doc=1 parcela=3"), gerados)
        assertEquals(SituacaoBoleto.ERRO, r.single().situacao)
        assertTrue(r.single().mensagem.contains("parcela 2: BankPlus recusou"), r.single().mensagem)
    }

    @Test
    fun `falha da BankPlus numa nota nao derruba a outra`() {
        val r = servico(bankPlus(listaFalha = setOf(1))).gerar(10, listOf(nota(1), nota(2)))
        assertEquals(listOf(SituacaoBoleto.ERRO, SituacaoBoleto.GERADO), r.map { it.situacao })
        assertTrue(r[0].mensagem.contains("Read timed out"), r[0].mensagem)
        assertEquals(listOf("doc=2 parcela=1"), gerados)
    }

    @Test
    fun `nota sem parcela no SAP vira erro`() {
        val semParcela = nota(1).also { it.documentInstallments = null }
        val r = servico().gerar(10, listOf(semParcela))
        assertEquals(SituacaoBoleto.ERRO, r.single().situacao)
        assertEquals(listOf<String>(), gerados)
    }

    @Test
    fun `segunda geracao da mesma ordem em andamento e recusada`() {
        val entrou = CountDownLatch(1)
        val libera = CountDownLatch(1)
        val s = servico(bankPlus(aoListar = { entrou.countDown(); libera.await(5, TimeUnit.SECONDS) }))
        val primeira = Thread { s.gerar(10, listOf(nota(1))) }.also { it.start() }
        assertTrue(entrou.await(5, TimeUnit.SECONDS))

        val e = assertThrows(Exception::class.java) { s.gerar(10, listOf(nota(1))) }
        assertTrue(e.message!!.contains("já estão sendo gerados"), e.message)

        libera.countDown()
        primeira.join(5000)
        // terminou: a trava solta e a ordem pode gerar de novo
        s.gerar(10, listOf(nota(1)))
        assertEquals(listOf("doc=1 parcela=1", "doc=1 parcela=1"), gerados)
    }
}
