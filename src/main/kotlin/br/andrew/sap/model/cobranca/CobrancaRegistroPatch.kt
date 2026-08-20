package br.andrew.sap.model.cobranca

import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonAnyGetter

/**
 * Payload parcial de @COB_TITULO pra usar dentro de um batch: o PATCH manda so os campos mexidos
 * (um mapa), mas o changeset precisa do Code pra montar a URL.
 *
 * O @JsonAnyGetter faz o JSON sair como o mapa cru, sem objeto envelope em volta - o corpo tem
 * que ser exatamente o que o Service Layer espera. getId() ja e @JsonIgnore na interface, e vai
 * entre apostrofos porque Code de UDT e alfanumerico (mesma forma de Regiao.getId()).
 *
 * Pra operacao sem corpo (DELETE) use BatchIdOnly - nao e trabalho desta classe.
 */
data class CobrancaRegistroPatch(
    private val code: String,
    @get:JsonAnyGetter val campos: Map<String, Any?>,
) : BatchId {
    override fun getId(): String = "'$code'"
}
