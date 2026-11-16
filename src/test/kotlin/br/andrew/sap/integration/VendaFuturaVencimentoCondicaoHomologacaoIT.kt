package br.andrew.sap.integration

import JournalEntry
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.schedules.futura.ReclassificacaoEntregaVendaFuturaSchedule

import br.andrew.sap.services.comercial.ContratoVendaFuturaService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Escreve no SAP HOMOLOG. O VFET criado e cancelado no fim; o adiantamento de R$ 1,00 NAO tem como
 * ser desfeito pela API (ver descarta) e fica aberto, so desligado do contrato - devolver pelo client.
 * Schedules desligados, views nao reprovisionadas e BankPlus mockado: o BankPlus do HMG aponta para PRODUCAO.
 * Rodar: RUN_SAP_HMG_WRITE=1 ./gradlew test --tests '*VendaFuturaVencimentoCondicaoHomologacaoIT'
 */
@SpringBootTest(properties = ["jobs.scheduling.enabled=false", "sql=false"])
//o application.properties de teste esconde o principal, que e o que aponta para o HOMOLOG
@TestPropertySource(locations = ["file:src/main/resources/application.properties"])
@EnabledIfEnvironmentVariable(named = "RUN_SAP_HMG_WRITE", matches = "1")
class VendaFuturaVencimentoCondicaoHomologacaoIT {

    @MockitoBean lateinit var bankplus: BankPlusService
    @Autowired lateinit var adiantamentos: DownPaymentService
    @Autowired lateinit var lancamentos: JournalEntriesService
    @Autowired lateinit var contratos: ContratoVendaFuturaService
    @Autowired lateinit var pedidos: OrdersService
    @Value("\${sap.service.layer.companyDB}") lateinit var empresa: String
    @Value("\${venda-futura.conta-controle}") lateinit var contaControle: String

    //o schedule e @Profile("!test"): montado a mao, novaReclassificacao so usa adiantamentos e a conta controle
    private val reclassificacao by lazy {
        ReclassificacaoEntregaVendaFuturaSchedule(mock(), mock(), mock(), mock(), mock(), mock(), mock(),
            adiantamentos, mock(), mock(), listOf(), "", 79, contaControle)
    }

    @BeforeEach
    fun soHomolog() = require(empresa == "HOMOLOG") { "Este teste escreve no SAP: aceita somente HOMOLOG, veio $empresa" }

    /** Ultimo boleto de um contrato que tem boleto. */
    private fun boletoDeContrato(): Document =
        adiantamentos.get(Filter(Predicate("U_venda_futura", 0, Condicao.GREAT)), OrderBy("DocEntry", Order.DESC))
            .tryGetValues<Document>().first { it.U_venda_futura != null && it.controlAccount != null }

    @Test
    fun `reclassificacao nasce vencendo no ultimo boleto do contrato`() {
        val boleto = boletoDeContrato()
        val contrato = boleto.U_venda_futura!!
        val ultimo = adiantamentos.getLastInstallment(contrato)
        assertNotNull(ultimo)
        val entrega = Invoice(boleto.CardCode, null, listOf(), boleto.getBPL_IDAssignedToInvoice()).also {
            it.U_venda_futura = contrato
            it.DocTotal = "1.00"
            it.controlAccount = boleto.controlAccount
            it.docNum = "IT${System.currentTimeMillis() % 1_000_000_000}"
        }

        val jdt = lancamentos.save(reclassificacao.novaReclassificacao(entrega)).tryGetValue<JournalEntry>().JdtNum!!
        try {
            val salvo = lancamentos.getById(jdt)
            @Suppress("UNCHECKED_CAST")
            val linhas = salvo["JournalEntryLines"] as List<Map<String, Any?>>
            println("HOMOLOG VFET $jdt contrato $contrato: ultimo boleto $ultimo, lancamento ${salvo["ReferenceDate"]}, " +
                "vencimento ${salvo["DueDate"]}, linhas ${linhas.map { it["DueDate"] }}")
            assertEquals(ultimo, salvo["DueDate"].toString().take(10))
            linhas.forEach { assertEquals(ultimo, it["DueDate"].toString().take(10)) }
        } finally {
            lancamentos.cancel(jdt.toString())
            println("HOMOLOG VFET $jdt cancelado")
        }
    }

    @Test
    fun `adiantamento sai com condicao e forma do pedido`() {
        val contrato = contratos.getById(boletoDeContrato().U_venda_futura!!).tryGetValue<Contrato>()
        val pedido = pedidos.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()

        val adt = adiantamentos.adiantamentosVendaFuturaSave(contrato,
            PaymentDueDates(BigDecimal("1.00"), LocalDate.now().plusDays(30))).docEntry!!
        try {
            val salvo = adiantamentos.getById(adt)
            println("HOMOLOG adiantamento $adt contrato ${contrato.DocEntry}: pedido condicao ${pedido.paymentGroupCode} " +
                "forma ${pedido.paymentMethod} | adiantamento condicao ${salvo["PaymentGroupCode"]} forma ${salvo["PaymentMethod"]}")
            assertEquals(pedido.paymentGroupCode, (salvo["PaymentGroupCode"] as Number).toInt())
            if (!pedido.paymentMethod.isNullOrBlank())
                assertEquals(pedido.paymentMethod, salvo["PaymentMethod"])
        } finally {
            descarta(adt)
        }
    }

    /**
     * shortcut: adiantamento nao aceita Cancel nem Close no Service Layer (-5006) e a devolucao
     * (CreditNotes) falha no HOMOLOG por CFOP da utilizacao. So desliga do contrato; o documento de
     * R$ 1,00 fica aberto e precisa ser devolvido pelo client do SAP.
     */
    private fun descarta(adt: Int) {
        adiantamentos.update(mapOf("U_venda_futura" to null), adt.toString())
        println("HOMOLOG adiantamento $adt desligado do contrato - DEVOLVER PELO CLIENT DO SAP")
    }
}
