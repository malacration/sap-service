package br.andrew.sap.services.security.acesso

import br.andrew.sap.services.security.Rule
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/** Uma regra de um perfil: URL (padrao do rules.yml), metodos liberados e uma nota opcional. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegraDoc(
    val url: String = "",
    val actions: List<String> = emptyList(),
    val comentario: String? = null,
) {
    fun paraRule() = Rule(url, actions)
}

/**
 * O conjunto completo das regras num instante: perfil -> regras. Cada versao gravada no SAP e um
 * documento inteiro, por isso historico, diff e restaurar saem direto das linhas.
 *
 * `schema` existe para uma imagem antiga conseguir reconhecer (e ignorar campos de) um documento
 * gravado por uma imagem mais nova durante um deploy `start-first` ou um rollback.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RegrasDocumento(
    val schema: Int = 1,
    val perfis: Map<String, List<RegraDoc>> = emptyMap(),
) {
    fun paraRegras(): Map<String, List<Rule>> =
        perfis.mapValues { (_, regras) -> regras.map { it.paraRule() } }
}

/** JSON do documento, como fica no campo memo do SAP. Leitura tolerante a campos desconhecidos. */
object RegrasCodec {
    private val mapper = jacksonObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    fun toJson(documento: RegrasDocumento): String = mapper.writeValueAsString(documento)

    fun fromJson(json: String): RegrasDocumento = mapper.readValue(json, RegrasDocumento::class.java)
}
