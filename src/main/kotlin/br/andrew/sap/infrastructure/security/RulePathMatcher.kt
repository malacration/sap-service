package br.andrew.sap.infrastructure.security

import br.andrew.sap.services.security.Rule
import java.util.concurrent.ConcurrentHashMap

/**
 * Casamento de URL e metodo contra as regras de acesso.
 *
 * Sai do RoleBasedAuthorizationFilter para o filtro e o simulador da tela de regras usarem
 * exatamente o mesmo codigo: o "Testar acesso" so serve se responder o que o filtro responde.
 *
 * O padrao NAO vira mais um regex. Antes virava (um asterisco em "qualquer coisa menos barra", dois
 * em "qualquer coisa"), e regex tem backtracking: um padrao valido com poucos curingas misturados
 * levava segundos para rejeitar um caminho de 3 KB, o que deixava uma regra gravada pela tela
 * ocupar as threads HTTP. Aqui o casamento e por programacao dinamica, com custo proporcional a
 * (tamanho do padrao x tamanho do caminho), qualquer que seja o padrao.
 *
 * O resultado e o mesmo do regex antigo (RulePathMatcherEquivalenciaTest compara os dois): um
 * asterisco casa qualquer coisa menos barra, dois ou mais asteriscos seguidos casam qualquer coisa,
 * e o ponto do padrao casa qualquer caractere. As arestas antigas foram preservadas de proposito: o
 * sufixo de barra e dois asteriscos e um startsWith sem fronteira de barra, e o ponto nao e escapado.
 */
class RulePathMatcher(private val context: String = "") {

    private val programas = ConcurrentHashMap<String, IntArray>()

    /** Primeira regra que libera o metodo neste caminho, ou null se nenhuma libera. */
    fun autoriza(regras: List<Rule>, metodo: String, caminho: String): Rule? =
        regras.firstOrNull { casa(it, metodo, caminho) }

    fun casa(rule: Rule, metodo: String, caminho: String): Boolean =
        matchPath(rule.url, caminho) &&
            (rule.actions.any { it.equals(metodo, ignoreCase = true) } || rule.actions.contains("*"))

    fun matchPath(pattern: String, path: String): Boolean {
        val adjustedPattern = normalizePath(pattern)
        val adjustedPath = normalizePath(path)

        if (adjustedPattern.endsWith("/**")) {
            return adjustedPath.startsWith(adjustedPattern.removeSuffix("/**"))
        }
        return casaPrograma(programaDe(adjustedPattern), adjustedPath)
    }

    /**
     * Mensagem de erro se o padrao tem caracteres que o filtro so trataria de forma literal (e que,
     * portanto, nunca casariam com um endpoint de verdade, como uma variavel entre chaves); null se ok.
     */
    fun problemaDoPadrao(pattern: String): String? {
        val estranhos = normalizePath(pattern).filterNot { (it.isLetterOrDigit() && it.code < 128) || it in PERMITIDOS }
        return if (estranhos.isEmpty()) null
        else "padrao com caractere(s) nao suportado(s): ${estranhos.toSet().joinToString(" ")} (use letras, numeros e / _ - . *)"
    }

    // ------------------------------------------------------------------ casamento

    /**
     * Padroes vem tambem de rascunhos e da cobertura da tela: o cache tem teto para nao crescer sem limite.
     * Os padroes em uso de verdade sao poucas centenas e voltam a ser compilados em microssegundos.
     */
    private fun programaDe(padrao: String): IntArray {
        if (programas.size >= TETO_DO_CACHE) programas.clear()
        return programas.getOrPut(padrao) { compilar(padrao) }
    }

    /** Programa: valor >= 0 e um ponto de codigo literal; SIMPLES e um asterisco; DUPLO e uma corrida de 2 ou mais. */
    private fun compilar(padrao: String): IntArray {
        val pontos = padrao.codePoints().toArray()
        val programa = ArrayList<Int>(pontos.size)
        var i = 0
        while (i < pontos.size) {
            if (pontos[i] == ASTERISCO) {
                var fim = i
                while (fim < pontos.size && pontos[fim] == ASTERISCO) fim++
                // O regex antigo trocava os pares "**" por ".*" e deixava o "*" que sobrava como "menos barra":
                // uma corrida impar (3, 5...) era "qualquer coisa" seguido de "menos barra" (a unica diferenca e
                // o terminador de linha, que so o segundo aceita). Mantido para a equivalencia ser exata.
                val tamanho = fim - i
                if (tamanho >= 2) {
                    programa.add(DUPLO)
                    if (tamanho % 2 == 1) programa.add(SIMPLES)
                } else {
                    programa.add(SIMPLES)
                }
                i = fim
            } else {
                programa.add(pontos[i])
                i++
            }
        }
        return programa.toIntArray()
    }

    /** `atual[j]` = o trecho ja lido do padrao casa os `j` primeiros caracteres do caminho. */
    private fun casaPrograma(programa: IntArray, texto: String): Boolean {
        // Por ponto de codigo (como o regex antigo): um emoji e um caractere so, nao dois.
        val caminho = texto.codePoints().toArray()
        val n = caminho.size
        var atual = BooleanArray(n + 1)
        var proximo = BooleanArray(n + 1)
        atual[0] = true
        for (token in programa) {
            proximo.fill(false)
            var alguma = false
            when (token) {
                DUPLO -> {
                    proximo[0] = atual[0]
                    alguma = proximo[0]
                    for (j in 1..n) {
                        proximo[j] = atual[j] || (proximo[j - 1] && !terminadorDeLinha(caminho[j - 1]))
                        alguma = alguma || proximo[j]
                    }
                }
                SIMPLES -> {
                    proximo[0] = atual[0]
                    alguma = proximo[0]
                    for (j in 1..n) {
                        proximo[j] = atual[j] || (proximo[j - 1] && caminho[j - 1] != BARRA)
                        alguma = alguma || proximo[j]
                    }
                }
                else -> for (j in 1..n) {
                    val c = caminho[j - 1]
                    proximo[j] = atual[j - 1] && (c == token || (token == PONTO && !terminadorDeLinha(c)))
                    alguma = alguma || proximo[j]
                }
            }
            // Nenhum prefixo do caminho casa mais: nao adianta ler o resto do padrao (caminho longo e sem chance).
            if (!alguma) return false
            val troca = atual
            atual = proximo
            proximo = troca
        }
        return atual[n]
    }

    // No regex antigo o ponto e o "qualquer coisa" nao casavam terminador de linha; o "menos barra" casava. Mantido igual.
    private fun terminadorDeLinha(c: Int) = c == 0x0A || c == 0x0D || c == 0x85 || c == 0x2028 || c == 0x2029

    private fun normalizePath(p: String): String {
        if (p.isEmpty()) return "/"
        val withLeading = if (p.startsWith("/")) p else "/$p"
        return removeContext(withLeading.replace(BARRAS_REPETIDAS, "/"))
    }

    private fun removeContext(path: String): String {
        val ctxRaw = context.trim()
        if (ctxRaw.isEmpty()) return path
        val ctx = "/" + ctxRaw.trim('/')
        if (path == ctx) return "/"
        return if (path.startsWith("$ctx/")) {
            path.removePrefix(ctx).let { if (it.isEmpty()) "/" else it }
        } else {
            path
        }
    }

    private companion object {
        const val SIMPLES = -1
        const val DUPLO = -2
        const val TETO_DO_CACHE = 2000
        const val ASTERISCO = '*'.code
        const val BARRA = '/'.code
        const val PONTO = '.'.code
        val BARRAS_REPETIDAS = Regex("/+")
        val PERMITIDOS = setOf('/', '_', '-', '.', '*')
    }
}
