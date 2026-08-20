package br.andrew.sap.services.batch

import br.andrew.sap.services.abstracts.EntitiesService

/**
 * Uma operacao dentro do changeset. Existe pra carregar [headers] por requisicao, que o
 * Triple<method, payload, service> nao tinha lugar pra guardar.
 *
 * O caso que pediu isso: PATCH que substitui colecao filha precisa de
 * B1S-ReplaceCollectionsOnPatch, senao o Service Layer faz merge e devolve 200 sem apagar a
 * linha omitida (ver EntitiesService.updateReplacingCollections).
 */
data class BatchItem(
    val method: BatchMethod,
    val payload: Any,
    val service: EntitiesService<*>,
    val headers: Map<String, String> = emptyMap(),
)
