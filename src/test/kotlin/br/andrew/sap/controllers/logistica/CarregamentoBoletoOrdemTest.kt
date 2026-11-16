package br.andrew.sap.controllers.logistica

import br.andrew.sap.infrastructure.BoletoIdsConfig
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.enums.CancelStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.fiscal.OrigemBoletoEnum
import br.andrew.sap.services.logistica.CarregamentoBoletoService
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.logistica.SituacaoBoleto
import br.andrew.sap.services.documents.DeliveryService
import br.andrew.sap.services.documents.InvoiceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Ordem de carregamento com varios pedidos: cada pedido vira uma NF (mais a entrega de NF de venda
 * futura e, se uma NF foi estornada, o documento de cancelamento com a UDF da ordem). Passa pelo
 * controller inteiro: notas da ordem -> BankPlus -> historico -> zip.
 */
class CarregamentoBoletoOrdemTest {

    private val ordem = 77

    private fun doc(docEntry: Int, forma: String?, parcelas: Int, tipo: DocumentTypes = DocumentTypes.oInvoices,
                    cancel: CancelStatus = CancelStatus.csNo) =
        Document("C$docEntry", null, listOf(), "2").also { d ->
            d.docEntry = docEntry
            d.SequenceSerial = "${docEntry + 4000}"
            d.paymentMethod = forma
            d.docObjectCode = tipo
            d.CancelStatus = cancel
            d.documentInstallments = (1..parcelas).map { n -> Installment(null, 100.0).also { it.InstallmentId = n } }
        }

    private fun boleto(docEntry: Int, parcela: Int, status: String = "Confirmado") = Boleto().also {
        it.id = docEntry * 10 + parcela; it.numeroDaParcela = parcela; it.statusDescricao = status
    }

    private val notas = listOf(
        doc(1001, "BB-RC-BOL-1199", 3),                 // pedido A: 3 parcelas, nenhum boleto
        doc(1002, "BB-RC-BL-126455", 3),                // pedido B: parcela 1 ativa, 2 cancelada (paga por PIX), 3 sem boleto
        doc(1003, "PIX-RC", 1),                         // pedido C: PIX
        doc(1004, "BB-RC-BOL-1199", 2),                 // pedido D: BankPlus recusa a parcela 2
        doc(1005, "BB-RC-BOL-1199", 2),                 // pedido E: ja tinha tudo
        doc(1006, "BB-RC-BOL-1199", 1, cancel = CancelStatus.csCancellationDocument), // estorno de uma NF
    )
    private val entregas = listOf(doc(2001, "BB-RC-BOL-1199", 1, tipo = DocumentTypes.oDeliveryNotes))

    // BankPlus com estado: o que for gerado passa a aparecer na listagem (e no zip).
    private val naBankPlus = mutableMapOf(
        1002 to mutableListOf(boleto(1002, 1), boleto(1002, 2, "Cancelado")),
        1005 to mutableListOf(boleto(1005, 1), boleto(1005, 2)),
    )
    private val gerados = mutableListOf<String>()

    private val bankPlus = Mockito.mock(BankPlusService::class.java, Answer { inv ->
        when (inv.method.name) {
            "getBoletosBy" -> if (inv.arguments.size != 4) null
                else naBankPlus[(inv.arguments[1] as String).toInt()]?.toList() ?: listOf<Boleto>()
            "geraBoletos" -> {
                val doc = inv.arguments[1] as Int
                val parcela = inv.arguments[2] as Int
                assertEquals(OrigemBoletoEnum.notafiscal, inv.arguments[3])
                gerados.add("$doc/$parcela")
                if (doc == 1004 && parcela == 2) throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                    HttpHeaders(), "{\"mensagem\":\"Sacado sem endereco\"}".toByteArray(), null)
                naBankPlus.getOrPut(doc) { mutableListOf() }.add(boleto(doc, parcela))
                ByteArray(0)
            }
            "getPdf" -> "%PDF ${inv.arguments[0]}".toByteArray()
            else -> null
        }
    })

    private val carregamentoService = mock<CarregamentoService>()
    private val invoiceService = mock<InvoiceService>().also {
        whenever(it.getAll(eq(Document::class.java), any<Filter>())).thenReturn(notas)
    }
    private val deliveryService = mock<DeliveryService>().also {
        whenever(it.getAll(eq(Document::class.java), any<Filter>())).thenReturn(entregas)
    }

    private val controller = CarregamentoController(carregamentoService, mock(), invoiceService, deliveryService,
        mock(), mock(), mock(), mock(), mock(), mock(),
        CarregamentoBoletoService(bankPlus, BoletoIdsConfig(listOf("BB-RC-BOL-1199", "BB-RC-BL-126455")), odbcTudoAberto()), true)

    // INV6: toda parcela em aberto (Status 'O').
    private fun odbcTudoAberto() = Mockito.mock(br.andrew.sap.services.odbc.OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        @Suppress("UNCHECKED_CAST")
        val docs = (inv.arguments[1] as Map<String, Any?>)["docEntries"] as List<Int>
        br.andrew.sap.services.odbc.QueryResponse(rows = docs.flatMap { d -> (1..12).map { mapOf("DocEntry" to d, "InstlmntID" to it) } })
    })

    @Test
    fun `ordem com varios pedidos gera so o que falta e registra no historico`() {
        val resultado = controller.gerarBoletos(ordem, null)

        assertEquals(listOf("1001/1", "1001/2", "1001/3", "1002/3", "1004/1", "1004/2"), gerados)
        assertEquals(mapOf(
            "NF 5001" to SituacaoBoleto.GERADO,
            "NF 5002" to SituacaoBoleto.GERADO,
            "NF 5003" to SituacaoBoleto.NAO_E_BOLETO,
            "NF 5004" to SituacaoBoleto.ERRO,
            "NF 5005" to SituacaoBoleto.JA_TINHA,
        ), resultado.associate { it.nota to it.situacao })
        assertEquals("parcela 2: HTTP 400 - Sacado sem endereco", resultado.first { it.nota == "NF 5004" }.mensagem)
        verify(carregamentoService).update(mapOf("U_usuario" to "desconhecido",
            "U_acao" to "Boletos: 2 nota(s) gerada(s), 1 já tinha(m), 1 com erro"), "$ordem")
    }

    @Test
    fun `clicar de novo so tenta a parcela que falhou`() {
        controller.gerarBoletos(ordem, null)
        gerados.clear()

        val segunda = controller.gerarBoletos(ordem, null)

        assertEquals(listOf("1004/2"), gerados)
        assertEquals(SituacaoBoleto.JA_TINHA, segunda.first { it.nota == "NF 5001" }.situacao)
    }

    @Test
    fun `zip da ordem leva os boletos de todos os pedidos e nada da entrega nem do estorno`() {
        controller.gerarBoletos(ordem, null)

        val zip = controller.boletos(ordem).body!!
        val entradas = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(zip)).use { z ->
            generateSequence { z.nextEntry }.forEach { entradas[it.name] = String(z.readBytes()) }
        }

        assertEquals(listOf(
            "boleto-nota-5001-parcela1.pdf", "boleto-nota-5001-parcela2.pdf", "boleto-nota-5001-parcela3.pdf",
            "boleto-nota-5002-parcela1.pdf", "boleto-nota-5002-parcela3.pdf",
            "boleto-nota-5004-parcela1.pdf",
            "boleto-nota-5005-parcela1.pdf", "boleto-nota-5005-parcela2.pdf",
            "erros.txt",
        ), entradas.keys.toList())
        assertEquals("NF 5003: sem boleto na BankPlus", entradas["erros.txt"])
    }

    // Login pelo Keycloak: userName = preferred_username (o CPF); o nome de quem fez vai em _name.
    @Test
    fun `historico grava o nome de quem fez e nao o CPF`() {
        val usuario = br.andrew.sap.model.authentication.User("175", "MARIA DA SILVA",
            br.andrew.sap.model.authentication.UserOriginEnum.SalePerson, "12345678900", null, null, listOf(2), listOf("admin"))

        controller.gerarBoletos(ordem, usuario)

        verify(carregamentoService).update(mapOf("U_usuario" to "MARIA DA SILVA",
            "U_acao" to "Boletos: 2 nota(s) gerada(s), 1 já tinha(m), 1 com erro"), "$ordem")
    }
}
