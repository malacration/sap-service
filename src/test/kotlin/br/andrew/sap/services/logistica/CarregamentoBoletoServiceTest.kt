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
import br.andrew.sap.model.enums.CancelStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.io.ByteArrayInputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

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
                         geraFalha: Set<String> = setOf(), aoListar: () -> Unit = {}, pdfInvalido: Set<Int> = setOf(),
                         geraRecusa: Set<String> = setOf()) =
        mock(BankPlusService::class.java, Answer { inv ->
            when (inv.method.name) {
                "getBoletosBy" -> {
                    if (inv.arguments.size != 4) return@Answer null
                    assertEquals("2", inv.arguments[0])
                    assertEquals(OrigemBoletoEnum.notafiscal, inv.arguments[2])
                    assertEquals(true, inv.arguments[3], "listagem sempre com prazo")
                    aoListar()
                    val doc = (inv.arguments[1] as String).toInt()
                    if (doc in listaFalha) throw ResourceAccessException("I/O error on GET request for \"https://bankplus.interno/api/v2/x/cobranca\": Read timed out")
                    boletos[doc] ?: listOf<Boleto>()
                }
                "geraBoletos" -> {
                    assertEquals(2, inv.arguments[0])
                    assertEquals(OrigemBoletoEnum.notafiscal, inv.arguments[3])
                    assertEquals(true, inv.arguments.getOrNull(4), "geracao com prazo: POST pendurado nao pode prender a trava")
                    val chave = "doc=${inv.arguments[1]} parcela=${inv.arguments[2]}"
                    synchronized(gerados) { gerados.add(chave) }
                    if (chave in geraFalha) throw Exception("BankPlus recusou")
                    if (chave in geraRecusa) throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        HttpHeaders(), "{\"mensagem\":\"NF-e nao autorizada\"}".toByteArray(), null)
                    ByteArray(0)
                }
                "getPdf" -> {
                    assertEquals(true, inv.arguments[1], "pdf sempre com prazo")
                    val id = (inv.arguments[0] as String).toInt()
                    if (id in pdfInvalido) "<html>erro</html>".toByteArray() else "%PDF boleto $id".toByteArray()
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
        assertTrue(r[0].mensagem.contains("sem resposta do servico"), r[0].mensagem)
        assertFalse(r[0].mensagem.contains("https://"), "URL interna nao vai para a tela: ${r[0].mensagem}")
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

    private fun entradas(zip: ByteArray): Map<String, String> {
        val r = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(zip)).use { z ->
            generateSequence { z.nextEntry }.forEach { r[it.name] = String(z.readBytes()) }
        }
        return r
    }

    @Test
    fun `zip leva um pdf por boleto ativo`() {
        val zip = servico(bankPlus(mapOf(1 to listOf(boleto(1), boleto(2))))).zip(listOf(nota(1, parcelas = 2)))
        assertEquals(mapOf("boleto-nota-5001-parcela1.pdf" to "%PDF boleto 100", "boleto-nota-5001-parcela2.pdf" to "%PDF boleto 200"),
            entradas(zip))
    }

    @Test
    fun `boleto cancelado fica fora do zip`() {
        val zip = servico(bankPlus(mapOf(1 to listOf(boleto(1, "Cancelado"), boleto(2))))).zip(listOf(nota(1, parcelas = 2)))
        assertEquals(listOf("boleto-nota-5001-parcela2.pdf"), entradas(zip).keys.toList())
    }

    @Test
    fun `pdf invalido vai para erros txt`() {
        val zip = servico(bankPlus(mapOf(1 to listOf(boleto(1))), pdfInvalido = setOf(100))).zip(listOf(nota(1)))
        val e = entradas(zip)
        assertEquals(listOf("erros.txt"), e.keys.toList())
        assertTrue(e["erros.txt"]!!.contains("NF 5001 parcela 1"), e["erros.txt"])
    }

    @Test
    fun `nota sem boleto vai para erros txt sem derrubar as outras`() {
        val zip = servico(bankPlus(mapOf(2 to listOf(boleto(1))), listaFalha = setOf(3)))
            .zip(listOf(nota(1), nota(2), nota(3)))
        val e = entradas(zip)
        assertEquals(listOf("boleto-nota-5002-parcela1.pdf", "erros.txt"), e.keys.toList())
        assertTrue(e["erros.txt"]!!.contains("NF 5001: sem boleto na BankPlus"), e["erros.txt"])
        assertTrue(e["erros.txt"]!!.contains("NF 5003: consulta na BankPlus falhou (sem resposta do servico)"), e["erros.txt"])
        assertFalse(e["erros.txt"]!!.contains("https://"), "URL interna nao vai para o zip do cliente")
    }

    @Test
    fun `recusa da BankPlus mostra o motivo dela`() {
        val r = servico(bankPlus(geraRecusa = setOf("doc=1 parcela=1"))).gerar(10, listOf(nota(1)))
        assertEquals(SituacaoBoleto.ERRO, r.single().situacao)
        assertTrue(r.single().mensagem.contains("HTTP 400") && r.single().mensagem.contains("NF-e nao autorizada"), r.single().mensagem)
    }

    @Test
    fun `so NF valendo entra - entrega e documento de cancelamento ficam fora`() {
        fun doc(entry: Int, tipo: DocumentTypes, cancel: CancelStatus?) = nota(entry).also { it.docObjectCode = tipo; it.CancelStatus = cancel }
        val docs = listOf(doc(1, DocumentTypes.oInvoices, CancelStatus.csNo), doc(2, DocumentTypes.oInvoices, CancelStatus.csCancellationDocument),
            doc(3, DocumentTypes.oDeliveryNotes, CancelStatus.csNo), doc(4, DocumentTypes.oInvoices, null))
        assertEquals(listOf(1, 4), CarregamentoBoletoService.notasComBoleto(docs).map { it.docEntry })
    }
}
