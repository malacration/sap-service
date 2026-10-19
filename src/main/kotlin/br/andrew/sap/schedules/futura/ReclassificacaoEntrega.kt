package br.andrew.sap.schedules.futura

import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.transaction.TransactionCodeTypes
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Memo do lancamento de reclassificacao de uma entrega. Junto com o Ref1 (DocNum) e a chave que
 * identifica a entrega - DocNum sozinho se repete entre series/filiais. A
 * SBO_SP_VALIDACAO_VENDA_FUTURA (sap-sql) usa a mesma chave: mudar este texto exige mudar la.
 */
fun memoReclassificacaoEntrega(invoice: Invoice) =
    "Reclassificação entrega. Mercadoria venda futura [${invoice.U_venda_futura}]. NF Num ${invoice.docNum}"

/** Lancamento de reclassificacao (VFET ou VFEC) de uma entrega que nao foi estornado. */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReclassificacaoAtiva(val TransId: Int = 0, val TransCode: String = "")

/**
 * O que fazer com uma nota de entrega que o schedule de reclassificacao encontrou aberta.
 *
 * Existe porque a busca por referencia do JournalEntriesService.saveOrRecouverReference procura
 * so VFET, e a conciliacao troca o codigo para VFEC. Quando uma nota ja conciliada voltava a
 * aparecer aberta, nada era encontrado e o schedule lancava um segundo VFET da mesma entrega; a
 * conciliacao seguinte apropriava adiantamento do contrato de novo sobre mercadoria ja paga.
 * Caso real: contrato 585, NF 73782 - VFEC 1502448 e 1516918, o segundo consumiu os R$ 160 do
 * adiantamento de devolucao. A SBO_SP_VALIDACAO_VENDA_FUTURA (sap-sql) tambem barra esse lancamento.
 */
sealed class DecisaoReclassificacao {
    /** Nenhuma reclassificacao valida: lanca o VFET. */
    object Criar : DecisaoReclassificacao()

    /** VFET ainda nao conciliado: reaproveita em vez de lancar outro. */
    data class Reutilizar(val transId: Int) : DecisaoReclassificacao()

    /** Ja conciliada (VFEC): a entrega foi reclassificada e apropriada, nao ha o que refazer. */
    data class Pular(val transId: Int) : DecisaoReclassificacao()

    companion object {
        fun de(ativas: List<ReclassificacaoAtiva>): DecisaoReclassificacao {
            ativas.firstOrNull { it.TransCode == TransactionCodeTypes.VFEC.toString() }
                ?.let { return Pular(it.TransId) }
            ativas.firstOrNull { it.TransCode == TransactionCodeTypes.VFET.toString() }
                ?.let { return Reutilizar(it.TransId) }
            return Criar
        }
    }
}
