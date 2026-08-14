package br.andrew.sap.services.comercial

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.comercial.DebOrCredt
import br.andrew.sap.model.sap.comercial.ReconciliationRow
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.journal.JournalEntriesService
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class BaixaSpreadResultado(
    val DocEntryApropriado: Int?,
    val DocNumApropriado: String?,
    val Diferenca: BigDecimal,
    val JdtNumAjuste: Int?
)

/**
 * Item de preview da aba "Pendentes de Baixa": uma entrega já reclassificada (VFET) mas ainda não
 * conciliada (nunca virou VFEC), com a diferença que ApropriacaoAdiantamento calcularia hoje contra
 * os adiantamentos disponíveis — antes de o usuário decidir o spread e confirmar.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class EntregaPendenteBaixa(
    val DocEntry: Int?,
    val DocNum: String?,
    val ValorNota: BigDecimal,
    val ValorAplicavel: BigDecimal,
    val Diferenca: BigDecimal,
    val TemAdiantamento: Boolean
)

private sealed class VfetLookup {
    data class Encontrado(val journal: JournalEntry) : VfetLookup()
    object JaConciliada : VfetLookup()
    object NaoEncontrada : VfetLookup()
    object Ambigua : VfetLookup()
}

/**
 * Baixa manual de uma entrega de venda futura já reclassificada (VFET) mas cujo total ficou alguns
 * centavos acima dos adiantamentos disponíveis (erro de arredondamento de impostos no faturamento) —
 * caso em que ApropriacaoAdiantamento.get() nunca fecha exatamente e a reclassificação fica presa
 * para sempre (nunca vira VFEC). Aceita uma tolerância (spread) em R$ e lança a diferença, quando
 * existir, na conta de perda configurada. Ver plano em `venda-futura`.
 */
@Service
class BaixaSpreadVendaFuturaService(
    val invoiceService: InvoiceService,
    val journalEntriesService: JournalEntriesService,
    val adiantamentoService: DownPaymentService,
    val apropriacaoVendaFuturaService: ApropriacaoVendaFuturaService,
    @Value("\${venda-futura.conta-controle:}") val contaControle: String,
    @Value("\${venda-futura.conta-perda-spread:}") val contaPerdaSpread: String,
    @Value("\${venda-futura.spread-maximo:1.00}") val spreadMaximo: BigDecimal
) {

    fun listarPendentes(docEntryContrato: Int): List<EntregaPendenteBaixa> {
        val filter = Filter(
            Predicate("Cancelled", Cancelled.tNO, Condicao.EQUAL),
            Predicate("U_venda_futura", docEntryContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Close, Condicao.EQUAL),
        )
        return invoiceService.get(filter).tryGetValues<Invoice>()
            .filter { it.U_vf_estornada != 1 }
            .filter { it.DocumentLines.none { line -> line.BaseType == 203 } }
            .mapNotNull { avaliarPendencia(it) }
    }

    private fun avaliarPendencia(invoice: Invoice): EntregaPendenteBaixa? {
        val lookup = buscarVfet(invoice)
        if (lookup !is VfetLookup.Encontrado)
            return null

        val adiantamentos = adiantamentoService.adiantamentosAbertos(invoice)
        val resultado = ApropriacaoAdiantamento(invoice, adiantamentos).calcular()
        val valorNota = BigDecimal(invoice.DocTotal ?: "0").setScale(2, RoundingMode.HALF_UP)

        return EntregaPendenteBaixa(
            invoice.docEntry, invoice.docNum,
            valorNota, valorNota.subtract(resultado.diferenca), resultado.diferenca,
            resultado.downPayments.isNotEmpty()
        )
    }

    fun baixarComSpread(docEntry: Int, spread: BigDecimal, usuario: String): BaixaSpreadResultado {
        if (contaControle.isBlank() || contaPerdaSpread.isBlank())
            throw Exception("Configuração de venda-futura (conta-controle/conta-perda-spread) ausente neste ambiente")
        if (spread.signum() <= 0 || spread.compareTo(spreadMaximo) > 0)
            throw Exception("Spread deve ser maior que zero e no máximo R$ $spreadMaximo")

        val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()

        if ((invoice.U_venda_futura ?: 0) <= 0)
            throw Exception("A nota ${invoice.docNum} não pertence a um contrato de venda futura")
        if (invoice.U_vf_estornada == 1)
            throw Exception("A nota ${invoice.docNum} já foi estornada")
        if (invoice.Cancelled == Cancelled.tYES)
            throw Exception("A nota ${invoice.docNum} está cancelada")

        val journalReclassificado = when (val lookup = buscarVfet(invoice)) {
            is VfetLookup.Encontrado -> lookup.journal
            VfetLookup.JaConciliada -> throw Exception("A nota ${invoice.docNum} já está conciliada")
            VfetLookup.NaoEncontrada -> throw Exception("A nota ${invoice.docNum} ainda não possui reclassificação (VFET) para ser baixada")
            VfetLookup.Ambigua -> throw Exception("Mais de uma reclassificação (VFET) encontrada para a nota ${invoice.docNum}; resolva manualmente")
        }

        val adiantamentos = adiantamentoService.adiantamentosAbertos(invoice)
        if (adiantamentos.isEmpty())
            throw Exception("Não há adiantamento disponível para apropriar contra a nota ${invoice.docNum}")

        val resultado = ApropriacaoAdiantamento(invoice, adiantamentos).calcular()
        if (resultado.downPayments.isEmpty())
            throw Exception("Não há adiantamento aplicável à nota ${invoice.docNum}")
        if (resultado.diferenca.compareTo(spread) > 0)
            throw Exception("Diferença de R$ ${resultado.diferenca} entre os adiantamentos disponíveis e o total da nota é maior que o spread informado (R$ $spread)")

        val filial = invoice.getBPL_IDAssignedToInvoice().toIntOrNull()
            ?: throw Exception("Nota sem filial")

        var jdtNumAjuste: Int? = null
        val creditosExtras: List<ReconciliationRow> = if (resultado.diferenca.signum() != 0) {
            val cc = invoice.DocumentLines.firstOrNull { it.CostingCode != null && it.CostingCode2 != null }
                ?: throw Exception("Nota ${invoice.docNum} sem centro de custo nas linhas; não é possível lançar o ajuste de spread")

            val debPerda = JournalEntryLines(contaPerdaSpread, resultado.diferenca.toDouble(), 0.0, filial, cc.CostingCode, cc.CostingCode2)
            val credControle = JournalEntryLines(contaControle, 0.0, resultado.diferenca.toDouble(), filial, cc.CostingCode, cc.CostingCode2)
                .also { it.ShortName = invoice.CardCode }
            val ajusteEntry = JournalEntry(
                listOf(debPerda, credControle),
                "Ajuste de spread na baixa manual da venda futura. NF Num ${invoice.docNum} | Cont ${invoice.U_venda_futura} | Spread R$ $spread | por $usuario"
            ).also {
                it.TransactionCode = TransactionCodeTypes.AROU.toString()
                it.Reference = invoice.docNum
            }
            val ajuste = journalEntriesService.saveOrRecouverReference(ajusteEntry)
            val valorRecuperado = ajuste.getReconciliationRows(DebOrCredt.Credt)
                .sumOf { it.reconcileAmount() }
            if (BigDecimal.valueOf(valorRecuperado).compareTo(resultado.diferenca) != 0)
                throw Exception("Já existe um lançamento de ajuste (JdtNum ${ajuste.JdtNum}) para a nota ${invoice.docNum} com valor diferente do esperado; ajuste manualmente")

            jdtNumAjuste = ajuste.JdtNum
            ajuste.getReconciliationRows(DebOrCredt.Credt)
        } else {
            listOf()
        }

        val apropriado = apropriacaoVendaFuturaService.conciliar(
            invoice,
            journalReclassificado,
            resultado.downPayments,
            creditosExtras = creditosExtras,
            notaAdicionalMemo = "Baixa manual com spread R$ $spread por $usuario"
        )

        return BaixaSpreadResultado(apropriado.docEntry, apropriado.docNum, resultado.diferenca, jdtNumAjuste)
    }

    private fun buscarVfet(invoice: Invoice): VfetLookup {
        val filial = invoice.getBPL_IDAssignedToInvoice().toIntOrNull() ?: return VfetLookup.NaoEncontrada
        val candidatos = journalEntriesService.getAll(
            JournalEntry::class.java,
            Filter(Predicate("Reference", invoice.docNum ?: "", Condicao.EQUAL))
        )
        if (candidatos.any { it.TransactionCode == TransactionCodeTypes.VFEC.toString() })
            return VfetLookup.JaConciliada

        val vfetCandidatos = candidatos.filter {
            it.TransactionCode == TransactionCodeTypes.VFET.toString() && it.getFilial() == filial
        }
        return when {
            vfetCandidatos.isEmpty() -> VfetLookup.NaoEncontrada
            vfetCandidatos.size == 1 -> VfetLookup.Encontrado(vfetCandidatos.first())
            else -> {
                val valorNota = BigDecimal(invoice.DocTotal ?: "0").setScale(2, RoundingMode.HALF_UP)
                val exato = vfetCandidatos.firstOrNull { candidato ->
                    val total = candidato.getReconciliationRows(DebOrCredt.Debit).sumOf { it.reconcileAmount() }
                    BigDecimal.valueOf(total).compareTo(valorNota) == 0
                }
                exato?.let { VfetLookup.Encontrado(it) } ?: VfetLookup.Ambigua
            }
        }
    }
}
