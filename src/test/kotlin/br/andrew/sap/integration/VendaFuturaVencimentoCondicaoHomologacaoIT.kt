package br.andrew.sap.integration

import JournalEntry
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.payment.PaymentTermsLines
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

import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.comercial.ContratoVendaFuturaService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId

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
    @Autowired lateinit var condicoes: PaymentTermsTypesService
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
    fun `reclassificacao nasce vencendo no ultimo boleto do contrato, nunca antes de hoje`() {
        val boleto = boletoDeContrato()
        val contrato = boleto.U_venda_futura!!
        //nulo e valido (todos os boletos cancelados/devolvidos): vence hoje
        val ultimo = adiantamentos.getLastInstallment(contrato)
        val entrega = Invoice(boleto.CardCode, null, listOf(), boleto.getBPL_IDAssignedToInvoice()).also {
            it.U_venda_futura = contrato
            it.DocTotal = "1.00"
            it.controlAccount = boleto.controlAccount
            it.docNum = "IT${System.currentTimeMillis() % 1_000_000_000}"
        }

        //hoje capturado uma vez: o mesmo valor vai para a producao e para o esperado (sem corrida na virada do dia)
        val hoje = LocalDate.now(ZoneId.of("America/Porto_Velho"))
        //calculado aqui, sem o helper de producao
        val esperado = (ultimo?.take(10)?.let { maxOf(LocalDate.parse(it), hoje) } ?: hoje).toString()
        val jdt = lancamentos.save(reclassificacao.novaReclassificacao(entrega, hoje)).tryGetValue<JournalEntry>().JdtNum!!
        try {
            val salvo = lancamentos.getById(jdt)
            @Suppress("UNCHECKED_CAST")
            val linhas = salvo["JournalEntryLines"] as List<Map<String, Any?>>
            println("HOMOLOG VFET $jdt contrato $contrato: ultimo boleto $ultimo, lancamento ${salvo["ReferenceDate"]}, " +
                "hoje $hoje, esperado $esperado, vencimento ${salvo["DueDate"]}, linhas ${linhas.map { it["DueDate"] }}")
            assertEquals(2, linhas.size, "o SAP deve devolver as duas linhas do lancamento")
            assertEquals(esperado, salvo["DueDate"].toString().take(10))
            linhas.forEach { assertEquals(esperado, it["DueDate"].toString().take(10)) }
        } finally {
            lancamentos.cancel(jdt.toString())
            println("HOMOLOG VFET $jdt cancelado")
        }
    }

    /**
     * Contrato cujo pedido tem condicao com mais de uma parcela (CTG1), com o pedido e as parcelas.
     * HMG_CONDICAO_MULTIPARCELA=<GroupNum> forca a condicao no pedido (so em memoria, nada e salvo
     * no pedido) do contrato do ultimo boleto.
     */
    private fun contratoComCondicaoMultiparcela(): Triple<Contrato, OrderSales, List<PaymentTermsLines>> {
        val forcada = System.getenv("HMG_CONDICAO_MULTIPARCELA")?.takeIf { it.isNotBlank() }
        if (forcada != null) {
            val condicao = forcada.toIntOrNull() ?: throw AssertionError("HMG_CONDICAO_MULTIPARCELA deve ser o GroupNum numerico, veio '$forcada'")
            val parcelas = condicoes.getParcelas(condicao)
            if (parcelas.size <= 1)
                throw AssertionError("HMG_CONDICAO_MULTIPARCELA=$condicao tem ${parcelas.size} parcela(s) em CTG1: informe uma com mais de uma")
            val contrato = contratos.getById(boletoDeContrato().U_venda_futura!!).tryGetValue<Contrato>()
            val pedido = pedidos.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
            println("HOMOLOG condicao forcada por HMG_CONDICAO_MULTIPARCELA: pedido ${pedido.docEntry} tinha ${pedido.paymentGroupCode}, usando $condicao")
            pedido.paymentGroupCode = condicao
            return Triple(contrato, pedido, parcelas)
        }
        val parcelasPorCondicao = mutableMapOf<Int, List<PaymentTermsLines>>()
        val vistos = mutableSetOf<Int>()
        val boletos = adiantamentos.get(Filter(Predicate("U_venda_futura", 0, Condicao.GREAT)),
            OrderBy("DocEntry", Order.DESC), Pageable.ofSize(100)).tryGetValues<Document>()
        for (id in boletos.mapNotNull { it.U_venda_futura }) {
            if (!vistos.add(id) || vistos.size > 40) continue
            val contrato = contratos.getById(id).tryGetValue<Contrato>()
            val pedido = pedidos.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
            val condicao = pedido.paymentGroupCode?.takeIf { it > 0 } ?: continue
            val parcelas = parcelasPorCondicao.getOrPut(condicao) { condicoes.getParcelas(condicao) }
            if (parcelas.size > 1) return Triple(contrato, pedido, parcelas)
        }
        throw AssertionError("Nenhum contrato de venda futura com pedido em condicao de mais de uma parcela entre ${vistos.size} " +
            "contratos (condicoes vistas: ${parcelasPorCondicao.mapValues { it.value.size }}). " +
            "Rode com HMG_CONDICAO_MULTIPARCELA=<GroupNum> de uma condicao com 2+ linhas em CTG1.")
    }

    /**
     * Finding 1 do PR #313: o adiantamento leva a condicao do pedido. Com condicao multiparcela, o SAP
     * recalcula o DocDueDate ou divide em parcelas? Vencimento hoje+37, que nenhuma condicao gera.
     * Imprime o que o SAP gravou ANTES de afirmar, para ver o comportamento mesmo quando falha.
     */
    @Test
    fun `adiantamento com condicao multiparcela do pedido mantem vencimento enviado em parcela unica`() {
        val (contrato, pedido, parcelas) = contratoComCondicaoMultiparcela()
        println("HOMOLOG contrato ${contrato.DocEntry} pedido ${pedido.docEntry} condicao ${pedido.paymentGroupCode} " +
            "forma ${pedido.paymentMethod}: ${parcelas.size} parcelas " +
            parcelas.joinToString { "${it.InstMonth}m+${it.InstDays}d ${it.InstPrcnt}%" })
        val vencimento = LocalDate.now(ZoneId.of("America/Porto_Velho")).plusDays(37).toString()

        val adt = adiantamentos.adiantamentosVendaFuturaSave(contrato,
            PaymentDueDates(BigDecimal("1.00"), LocalDate.parse(vencimento)), pedido).docEntry!!
        println("HOMOLOG adiantamento criado DocEntry $adt (vencimento enviado $vencimento)")
        try {
            val salvo = adiantamentos.getById(adt)
            @Suppress("UNCHECKED_CAST")
            val prestacoes = (salvo["DocumentInstallments"] as? List<Map<String, Any?>>).orEmpty()
            println("HOMOLOG adiantamento $adt DocNum ${salvo["DocNum"]}: condicao ${salvo["PaymentGroupCode"]} " +
                "forma ${salvo["PaymentMethod"]} | DocDueDate ${salvo["DocDueDate"]} | ${prestacoes.size} parcela(s): " +
                prestacoes.joinToString { "${it["DueDate"]} total ${it["Total"]} ${it["Percentage"]}%" })
            //nenhum boleto: o BankPlus do HMG aponta para PRODUCAO
            verifyNoInteractions(bankplus)

            assertEquals(pedido.paymentGroupCode, (salvo["PaymentGroupCode"] as Number).toInt())
            if (!pedido.paymentMethod.isNullOrBlank())
                assertEquals(pedido.paymentMethod, salvo["PaymentMethod"])
            assertEquals(vencimento, salvo["DocDueDate"].toString().take(10), "SAP recalculou o DocDueDate")
            assertEquals(1, prestacoes.size, "SAP dividiu o adiantamento em parcelas")
            assertEquals(vencimento, prestacoes.single()["DueDate"].toString().take(10), "SAP mudou o vencimento da parcela")
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
