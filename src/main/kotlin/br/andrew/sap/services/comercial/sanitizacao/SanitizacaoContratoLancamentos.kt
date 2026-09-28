package br.andrew.sap.services.comercial.sanitizacao

import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.batch.*
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/** Um changeset por reclassificação: o SAP reverte todas as operações se qualquer uma falhar. */
@Service
class SanitizacaoContratoLancamentos(
    private val batch: BatchService,
    private val invoices: InvoiceService,
    private val journals: JournalEntriesService,
    private val env: SapEnvrioment,
    private val rest: RestTemplate,
    private val auth: AuthService,
) {
    fun aplicar(item: ReclassificacaoSanitizacao) {
        require(item.podeAplicar) { "Reclassificação não elegível para correção." }
        val lote = BatchList()
        val destino = object : EntitiesService<Any>(env, rest, auth) {
            override fun path() = "/b1s/v1/InternalReconciliationsService_Cancel"
        }
        item.reconciliacoes.filter { it.cancelavel }.map { it.numero }.distinct().forEach { numero ->
            lote.add(BatchMethod.POST, mapOf("InternalReconciliationParams" to mapOf("ReconNum" to numero)), destino)
        }
        // As ações nativas Cancel lançam na data do documento original (conferido em HMG:
        // estorno e cancelamento saem com RefDate/DocDate do original). Não enviar parâmetros
        // de data: as ações não os aceitam. As datas são conferidas após o changeset.
        item.apropriacoes.filter { !it.cancelada }.forEach {
            lote.add(BatchMethod.CANCEL, ChaveSanitizacao(it.docEntry), invoices)
        }
        lote.add(BatchMethod.CANCEL, ChaveSanitizacao(item.transId), journals)
        val respostas = batch.run(lote)
        check(respostas.size == lote.size && respostas.all { it.success }) {
            "Resposta incompleta do SAP; confira o resultado antes de repetir."
        }
    }
}

private data class ChaveSanitizacao(val id: Int) : BatchId {
    override fun getId() = id.toString()
}
