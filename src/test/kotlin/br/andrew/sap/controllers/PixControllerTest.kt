package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.PixRequestAdiantamento
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestPixImmediate
import br.andrew.sap.model.uzzipay.RetonroPixQrCode
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class PixControllerTest {

    private val pixDynamicService = mock(DynamicPixQrCodeService::class.java)
    private val adiantamentoService = mock(DownPaymentService::class.java)

    private val controller = PixController(
        mock(TransactionsPixService::class.java),
        mock(InvoiceService::class.java),
        mock(PixPaymentVerificationService::class.java),
        pixDynamicService,
        adiantamentoService,
        "PIX-ITEM",
        66,
        0.0
    )

    private val user = User(
        "1",
        "Tester",
        UserOriginEnum.EmployeesInfo,
        "tester",
        bussinesPlace = listOf()
    )

    @BeforeEach
    fun setup() {
        PixRequestAdiantamento.clearContasConfiguradas()
        val conta = ContaUzziPayPix().also {
            it.idFilial = 3
            it.cnpj = "12345678000190"
            it.chavePix = "PIX-OK"
            it.privateKey = "private-key"
            it.consulta = "consulta"
            it.contaContabilBanco = "conta"
        }
        PixRequestAdiantamento.setUzziPayEnvrioment(UzziPayEnvrioment().also { it.contas = listOf(conta) })
        whenever(adiantamentoService.get(any<Filter>())).thenReturn(odata(listOf<Any>()))
    }

    @Test
    fun falhaNaUzzipayNaoCriaAdiantamento() {
        whenever(pixDynamicService.genereateFor(any<RequestPixImmediate>()))
            .thenThrow(RuntimeException("Falha de comunicação com a uzzipay"))

        val erro = assertThrows<RuntimeException> { controller.gerarChavePixByValor(request(), user) }

        assertTrue((erro.message ?: "").contains("uzzipay"))
        verify(adiantamentoService, never()).save(any())
    }

    @Test
    fun fluxoFelizGeraPixAntesDeSalvarAdiantamento() {
        whenever(pixDynamicService.genereateFor(any<RequestPixImmediate>())).thenReturn(retornoPix())
        whenever(adiantamentoService.save(any())).thenReturn(odataAdiantamentoSalvo())
        whenever(adiantamentoService.getById(321)).thenReturn(odataAdiantamentoComParcelas())

        val response = controller.gerarChavePixByValor(request(), user)

        assertEquals("ref-123", response.U_pix_reference)
        assertEquals(321, response.docEntry)
        val ordem = inOrder(pixDynamicService, adiantamentoService)
        ordem.verify(pixDynamicService).genereateFor(any<RequestPixImmediate>())
        ordem.verify(adiantamentoService).save(any())
        verify(adiantamentoService).updatePixInstallments(eq(321), any())
        verify(adiantamentoService, never()).estornarPorDevolucao(any(), any())
    }

    @Test
    fun falhaAoGravarPixNaParcelaEstornaPorDevolucao() {
        whenever(pixDynamicService.genereateFor(any<RequestPixImmediate>())).thenReturn(retornoPix())
        whenever(adiantamentoService.save(any())).thenReturn(odataAdiantamentoSalvo())
        whenever(adiantamentoService.getById(321)).thenReturn(odataAdiantamentoComParcelas())
        doThrow(RuntimeException("SAP retornou sucesso, mas não persistiu os dados PIX"))
            .whenever(adiantamentoService).updatePixInstallments(eq(321), any())

        val erro = assertThrows<RuntimeException> { controller.gerarChavePixByValor(request(), user) }

        assertTrue((erro.message ?: "").contains("não persistiu"))
        verify(adiantamentoService)
            .estornarPorDevolucao(any(), eq("Estorno automatico por falha ao gravar dados do PIX."))
    }

    @Test
    fun falhaNoEstornoOrientaDevolucaoManualComDocNum() {
        whenever(pixDynamicService.genereateFor(any<RequestPixImmediate>())).thenReturn(retornoPix())
        whenever(adiantamentoService.save(any())).thenReturn(odataAdiantamentoSalvo())
        whenever(adiantamentoService.getById(321)).thenReturn(odataAdiantamentoComParcelas())
        doThrow(RuntimeException("SAP retornou sucesso, mas não persistiu os dados PIX"))
            .whenever(adiantamentoService).updatePixInstallments(eq(321), any())
        whenever(adiantamentoService.estornarPorDevolucao(any(), any()))
            .thenThrow(RuntimeException("sem permissao"))

        val erro = assertThrows<Exception> { controller.gerarChavePixByValor(request(), user) }

        assertTrue((erro.message ?: "").contains("654"))
        assertTrue((erro.message ?: "").contains("devolucao manual"))
    }

    private fun request(): PixRequestAdiantamento {
        return PixRequestAdiantamento(
            cardCode = "C-001",
            valor = BigDecimal("82.00"),
            idFilial = 3,
            docEntry = 987,
            documentTypes = DocumentTypes.oOrders
        )
    }

    private fun retornoPix(): DataRetonroPixQrCode {
        return DataRetonroPixQrCode(RetonroPixQrCode("pix-copia-cola", "https://pix", "ref-123", null))
    }

    private fun odataAdiantamentoSalvo(): OData {
        return odata("""{"CardCode":"C-001","BPL_IDAssignedToInvoice":"3","DocEntry":321,"DocNum":"654"}""")
    }

    private fun odataAdiantamentoComParcelas(): OData {
        return odata(
            """{"CardCode":"C-001","BPL_IDAssignedToInvoice":"3","DocEntry":321,"DocNum":"654",
                "DocumentInstallments":[{"InstallmentId":1,"DueDate":"2026-07-17","Total":82.0}]}"""
        )
    }

    private fun odata(value: Any?): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = value
        return OData(backing)
    }
}
