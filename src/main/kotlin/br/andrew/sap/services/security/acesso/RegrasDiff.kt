package br.andrew.sap.services.security.acesso

data class MudancaRegra(val url: String, val antes: List<String>, val depois: List<String>)

data class MudancaPerfil(
    val perfil: String,
    /** NOVO, REMOVIDO ou ALTERADO. */
    val tipo: String,
    val adicionadas: List<RegraDoc>,
    val removidas: List<RegraDoc>,
    val alteradas: List<MudancaRegra>,
)

/** Comparacao entre dois documentos (por perfil e por URL) e a mescla usada na importacao. */
object RegrasDiff {

    /** So as diferencas de efeito: a mudanca so de comentario nao conta. */
    fun comparar(antes: RegrasDocumento?, depois: RegrasDocumento): List<MudancaPerfil> {
        val perfisAntes = antes?.perfis.orEmpty()
        val mudancas = mutableListOf<MudancaPerfil>()

        depois.perfis.forEach { (perfil, regrasDepois) ->
            val regrasAntes = perfisAntes[perfil]
            if (regrasAntes == null) {
                mudancas.add(MudancaPerfil(perfil, "NOVO", regrasDepois, emptyList(), emptyList()))
                return@forEach
            }
            val porUrlAntes = regrasAntes.associateBy { it.url }
            val porUrlDepois = regrasDepois.associateBy { it.url }
            val adicionadas = regrasDepois.filter { it.url !in porUrlAntes }
            val removidas = regrasAntes.filter { it.url !in porUrlDepois }
            val alteradas = regrasDepois
                .filter { porUrlAntes[it.url]?.let { antes -> antes.actions.toSet() != it.actions.toSet() } == true }
                .map { MudancaRegra(it.url, porUrlAntes.getValue(it.url).actions, it.actions) }
            if (adicionadas.isNotEmpty() || removidas.isNotEmpty() || alteradas.isNotEmpty())
                mudancas.add(MudancaPerfil(perfil, "ALTERADO", adicionadas, removidas, alteradas))
        }
        perfisAntes.filterKeys { it !in depois.perfis }.forEach { (perfil, regras) ->
            mudancas.add(MudancaPerfil(perfil, "REMOVIDO", emptyList(), regras, emptyList()))
        }
        return mudancas
    }

    /** Uma linha para a coluna "Resumo" do historico, ex.: `cobranca(+1), vendedor_admin(+1 ~2)`. */
    fun resumo(mudancas: List<MudancaPerfil>, limite: Int = 254): String {
        if (mudancas.isEmpty()) return "sem mudancas"
        val texto = mudancas.joinToString(", ") { m ->
            val partes = when (m.tipo) {
                "NOVO" -> listOf("novo +${m.adicionadas.size}")
                "REMOVIDO" -> listOf("removido -${m.removidas.size}")
                else -> listOfNotNull(
                    m.adicionadas.size.takeIf { it > 0 }?.let { "+$it" },
                    m.removidas.size.takeIf { it > 0 }?.let { "-$it" },
                    m.alteradas.size.takeIf { it > 0 }?.let { "~$it" },
                )
            }
            "${m.perfil}(${partes.joinToString(" ")})"
        }
        return if (texto.length <= limite) texto else texto.take(limite - 3) + "..."
    }

    /**
     * Uniao por perfil e por URL: nada do que ja existe e removido nem perde acao. Serve para
     * trazer as regras de outro arquivo (por exemplo o do repo) sem apagar o que so existe em producao.
     */
    fun mesclar(base: RegrasDocumento, extra: RegrasDocumento): RegrasDocumento {
        val resultado = LinkedHashMap<String, List<RegraDoc>>(base.perfis)
        extra.perfis.forEach { (perfil, regrasExtra) ->
            val regrasBase = resultado[perfil]
            if (regrasBase == null) {
                resultado[perfil] = regrasExtra
                return@forEach
            }
            val porUrl = LinkedHashMap<String, RegraDoc>()
            regrasBase.forEach { porUrl[it.url] = it }
            regrasExtra.forEach { nova ->
                val existente = porUrl[nova.url]
                porUrl[nova.url] = if (existente == null) nova else {
                    val acoes = (existente.actions + nova.actions).distinct().let { if ("*" in it) listOf("*") else it }
                    RegraDoc(existente.url, acoes, existente.comentario ?: nova.comentario)
                }
            }
            resultado[perfil] = porUrl.values.toList()
        }
        return base.copy(perfis = resultado)
    }
}
