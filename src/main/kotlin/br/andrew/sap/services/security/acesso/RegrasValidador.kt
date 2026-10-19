package br.andrew.sap.services.security.acesso

import br.andrew.sap.infrastructure.security.RulePathMatcher

class ResultadoValidacao(
    /** O documento normalizado (mesmo com erros, para a tela poder mostrar a previa). */
    val documento: RegrasDocumento,
    val erros: List<String>,
    val avisos: List<String>,
) {
    val ok: Boolean get() = erros.isEmpty()
}

/**
 * Regras de um documento de acesso. O que e erro bloqueia a gravacao; o que e aviso so aparece na tela.
 *
 * Tem que aceitar por inteiro o rules.yml do repo e o do swarm: o seed e a importacao nunca podem
 * falhar por causa de um arquivo que ja esta valendo em producao.
 */
object RegrasValidador {

    /** Perfis que o codigo verifica pelo nome. Podem ter as regras editadas, mas nao sair do cadastro. */
    val PERFIS_PROTEGIDOS = setOf(
        "admin", "vendedor_admin", "pix", "pix_admin", "cobranca", "business_partner", "liberacao_trava",
    )

    /** Verificados no codigo (User.kt) mas sem nenhuma regra nos arquivos: nunca sao concedidos via SSO. */
    private val PERFIS_SO_NO_CODIGO = setOf("caixa")

    /** Roles tecnicas do Keycloak: um perfil com esse nome valeria para todo usuario SSO. */
    private val NOMES_RESERVADOS = setOf("offline_access", "uma_authorization")
    private val PREFIXOS_RESERVADOS = listOf("filial-", "default-roles-")

    const val LIMITE_DOCUMENTO = 60_000

    private val NOME_PERFIL = Regex("^[a-z0-9][a-z0-9_-]*$")
    private val CHAVES = Regex("\\{[^/}]*}")
    private val CARACTERES_URL = Regex("^[A-Za-z0-9/_\\-.*]+$")
    private val VARIAVEL_COLADA = Regex("\\}[{*]|\\*\\{")
    private val TRES_OU_MAIS_ASTERISCOS = Regex("\\*{3,}")
    private val GRUPO_DE_ASTERISCOS = Regex("\\*+")

    /** Limites que mantem o regex do filtro barato: cada curinga a mais aumenta o custo do casamento. */
    private const val TAMANHO_MAXIMO_URL = 200
    private const val CURINGAS_MAXIMOS = 8
    private const val DUPLOS_MAXIMOS = 2
    private const val CURINGAS_POR_SEGMENTO = 2
    private val ACOES = setOf("get", "post", "put", "patch", "delete", "*")

    fun validar(documento: RegrasDocumento, matcher: RulePathMatcher = RulePathMatcher()): ResultadoValidacao {
        val erros = mutableListOf<String>()
        val avisos = mutableListOf<String>()
        val normalizado = LinkedHashMap<String, List<RegraDoc>>()

        documento.perfis.forEach { (perfil, regras) ->
            validarNomeDoPerfil(perfil, erros)
            if (regras.isEmpty()) {
                erros.add("Perfil '$perfil' sem regras: para tirar um perfil, exclua o perfil.")
                return@forEach
            }
            val porUrl = LinkedHashMap<String, RegraDoc>()
            regras.forEach { regra ->
                val nova = normalizarRegra(perfil, regra, matcher, erros, avisos) ?: return@forEach
                val existente = porUrl[nova.url]
                if (existente == null) {
                    porUrl[nova.url] = nova
                } else {
                    avisos.add("Perfil '$perfil': a URL '${nova.url}' aparece mais de uma vez e foi mesclada.")
                    porUrl[nova.url] = mesclarAcoes(existente, nova)
                }
            }
            normalizado[perfil] = porUrl.values.toList()
        }

        validarAdmin(normalizado, erros)
        (PERFIS_PROTEGIDOS - "admin").filter { it !in normalizado }.forEach {
            avisos.add("O perfil '$it' e usado pelo sistema mas nao tem regras.")
        }
        PERFIS_SO_NO_CODIGO.filter { it !in normalizado }.forEach {
            avisos.add("O perfil '$it' e verificado no codigo mas nao tem regras, entao nunca chega ao usuario via SSO.")
        }

        val resultado = documento.copy(perfis = normalizado)
        val tamanho = RegrasCodec.toJson(resultado).length
        if (tamanho > LIMITE_DOCUMENTO)
            erros.add("O documento de regras tem $tamanho caracteres; o limite e $LIMITE_DOCUMENTO.")
        return ResultadoValidacao(resultado, erros, avisos)
    }

    /** Erro de nome de perfil isolado, para as rotas que recebem o nome direto (PUT/DELETE /perfis/{perfil}). */
    fun erroDoNome(perfil: String): String? =
        mutableListOf<String>().also { validarNomeDoPerfil(perfil, it) }.firstOrNull()

    private fun validarNomeDoPerfil(perfil: String, erros: MutableList<String>) {
        if (!NOME_PERFIL.matches(perfil))
            erros.add("Perfil '$perfil': use letras minusculas, numeros, '_' ou '-', comecando por letra ou numero.")
        else if (perfil in NOMES_RESERVADOS || PREFIXOS_RESERVADOS.any { perfil.startsWith(it) })
            erros.add("Perfil '$perfil': nome reservado para roles tecnicas do Keycloak.")
    }

    private fun normalizarRegra(
        perfil: String,
        regra: RegraDoc,
        matcher: RulePathMatcher,
        erros: MutableList<String>,
        avisos: MutableList<String>,
    ): RegraDoc? {
        val original = regra.url.trim()
        val url = original.replace(CHAVES, "*")
        var valida = true

        if (!url.startsWith("/")) {
            erros.add("Perfil '$perfil': a URL '$original' precisa comecar com '/'.")
            valida = false
        } else if (!CARACTERES_URL.matches(url)) {
            erros.add("Perfil '$perfil': a URL '$original' so pode ter letras, numeros e / _ - . *")
            valida = false
        } else if (VARIAVEL_COLADA.containsMatchIn(original)) {
            // {x}{y} ou {x}* viraria "**", e "/a/**" faz startsWith("/a") (libera tambem "/ab"): ampliaria a regra digitada.
            // Variavel separada de outros curingas por uma barra ou texto (ex.: /a/{x}/**/b) continua valendo.
            erros.add("Perfil '$perfil': a URL '$original' tem variáveis {..} coladas em outra variável ou em *; separe-as com uma barra.")
            valida = false
        } else if (TRES_OU_MAIS_ASTERISCOS.containsMatchIn(url)) {
            // Nao normaliza em silencio: "/a/***" exige a barra final, mas "/a/**" vira startsWith("/a") e
            // passaria a liberar tambem "/ab". Melhor o admin escolher entre * e **.
            erros.add("Perfil '$perfil': a URL '$original' tem 3 ou mais asteriscos seguidos; use * (um trecho) ou ** (vários trechos).")
            valida = false
        } else if (url.length > TAMANHO_MAXIMO_URL) {
            erros.add("Perfil '$perfil': a URL passa de $TAMANHO_MAXIMO_URL caracteres.")
            valida = false
        } else if (GRUPO_DE_ASTERISCOS.findAll(url).count() > CURINGAS_MAXIMOS ||
            url.windowed(2).count { it == "**" } > DUPLOS_MAXIMOS ||
            url.split('/').any { GRUPO_DE_ASTERISCOS.findAll(it).count() > CURINGAS_POR_SEGMENTO }) {
            erros.add(
                "Perfil '$perfil': a URL '$original' tem curingas demais (no maximo $CURINGAS_MAXIMOS no total, " +
                    "$DUPLOS_MAXIMOS com ** e $CURINGAS_POR_SEGMENTO por trecho entre barras); isso deixaria o filtro lento."
            )
            valida = false
        } else {
            matcher.problemaDoPadrao(url)?.let {
                erros.add("Perfil '$perfil': a URL '$original' e um $it")
                valida = false
            }
        }
        if (valida && url != original)
            avisos.add("Perfil '$perfil': '$original' foi convertida para '$url' (o filtro usa * no lugar de {variavel}).")

        val acoes = regra.actions.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
        if (acoes.isEmpty()) {
            erros.add("Perfil '$perfil': a URL '$original' esta sem acoes.")
            valida = false
        }
        acoes.filter { it !in ACOES }.forEach {
            erros.add("Perfil '$perfil': a acao '$it' da URL '$original' nao existe (use get, post, put, patch, delete ou *).")
            valida = false
        }
        if (!valida) return null

        return RegraDoc(url, if ("*" in acoes) listOf("*") else acoes, regra.comentario?.trim()?.ifEmpty { null })
    }

    private fun mesclarAcoes(a: RegraDoc, b: RegraDoc): RegraDoc {
        val acoes = (a.actions + b.actions).distinct().let { if ("*" in it) listOf("*") else it }
        return RegraDoc(a.url, acoes, a.comentario ?: b.comentario)
    }

    private fun validarAdmin(perfis: Map<String, List<RegraDoc>>, erros: MutableList<String>) {
        val temCuringa = perfis["admin"].orEmpty().any { it.url == "/**" && it.actions == listOf("*") }
        if (!temCuringa)
            erros.add("O perfil 'admin' precisa manter a regra '/**' com todas as acoes, senao ninguem consegue corrigir as regras depois.")
    }
}
