package br.andrew.sap.services.security.acesso

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Importa e exporta as regras no formato do rules.yml.
 *
 * A exportacao sai SEMPRE no schema antigo (`roles` -> `url`/`actions`). O `RegrasArquivoService`
 * le o arquivo com um mapper estrito: uma chave a mais (um `comentario:`, por exemplo) derrubaria a
 * v6 e o modo `fonte=arquivo` a cada requisicao. As notas das regras saem como linhas `#`, que o
 * YAML ignora, e na importacao voltam a virar `comentario`.
 */
object RegrasYaml {

    private val mapper = YAMLMapper().registerKotlinModule()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    private val json = JsonMapper()

    private data class RegraYaml(
        val url: String = "",
        val actions: List<String> = emptyList(),
        val comentario: String? = null,
    )

    private data class ArquivoYaml(val roles: Map<String, List<RegraYaml>?>? = null)

    private val linhaComentario = Regex("""^\s*#\s?(.*)$""")
    private val linhaPerfil = Regex("""^ {2}([A-Za-z0-9_-]+):\s*(#.*)?$""")
    private val linhaUrl = Regex("""^\s*-\s*url:\s*["']?([^"'\s#]+)["']?""")

    /** Interpreta o YAML. Erros de sintaxe/estrutura viram IllegalArgumentException com texto para o usuario. */
    fun importar(texto: String): RegrasDocumento {
        val arquivo = try {
            mapper.readValue(texto, ArquivoYaml::class.java)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("YAML invalido: ${e.originalMessage}")
        }
        val roles = arquivo?.roles ?: throw IllegalArgumentException("YAML sem a chave 'roles'")
        val comentarios = extrairComentarios(texto)
        return RegrasDocumento(
            perfis = roles.mapValues { (perfil, regras) ->
                (regras ?: emptyList()).map {
                    RegraDoc(it.url, it.actions, it.comentario ?: comentarios[perfil to it.url])
                }
            }
        )
    }

    /** YAML no schema antigo, com as notas como linhas `#` logo acima da regra. */
    fun exportar(documento: RegrasDocumento): String = buildString {
        appendLine("roles:")
        documento.perfis.entries.forEachIndexed { indice, (perfil, regras) ->
            if (indice > 0) appendLine()
            appendLine("  $perfil:")
            regras.forEach { regra ->
                regra.comentario?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?.forEach { appendLine("    # $it") }
                appendLine("    - url: ${json.writeValueAsString(regra.url)}")
                appendLine("      actions: [${regra.actions.joinToString(", ") { json.writeValueAsString(it) }}]")
            }
        }
    }

    /**
     * As linhas `#` imediatamente acima de um `- url:` viram a nota dessa regra. Melhor esforco: uma
     * linha em branco ou qualquer outra coisa entre o comentario e a regra descarta o comentario.
     */
    private fun extrairComentarios(texto: String): Map<Pair<String, String>, String> {
        val resultado = LinkedHashMap<Pair<String, String>, String>()
        var perfil: String? = null
        val pendentes = mutableListOf<String>()
        for (linha in texto.lines()) {
            val comentario = linhaComentario.matchEntire(linha)
            val cabecalho = linhaPerfil.matchEntire(linha)
            val regra = linhaUrl.find(linha)
            when {
                comentario != null -> pendentes.add(comentario.groupValues[1].trim())
                cabecalho != null -> { perfil = cabecalho.groupValues[1]; pendentes.clear() }
                regra != null -> {
                    if (perfil != null && pendentes.isNotEmpty())
                        resultado[perfil to regra.groupValues[1]] = pendentes.joinToString(" ")
                    pendentes.clear()
                }
                else -> pendentes.clear()
            }
        }
        return resultado
    }
}
