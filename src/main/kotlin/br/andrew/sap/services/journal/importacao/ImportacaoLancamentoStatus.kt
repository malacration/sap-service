package br.andrew.sap.services.journal.importacao

import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonAnyGetter

/**
 * PATCH do status do log dentro do changeset dos lancamentos (mesmo padrao do CobrancaRegistroPatch):
 * o corpo e so o mapa de campos, e o Code vai na URL.
 */
class ImportacaoLancamentoStatus(private val code: String, status: String) : BatchId {
    @get:JsonAnyGetter
    val campos: Map<String, Any?> = mapOf("U_Status" to status)

    override fun getId(): String = "'$code'"
}
