package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RulePathMatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.random.Random

/**
 * O RulePathMatcher deixou de usar regex (backtracking) e passou a casar por programacao dinamica.
 * Este teste prova que o resultado e o mesmo: compara, em milhares de casos aleatorios, com a
 * implementacao antiga (copiada abaixo tal como estava no RoleBasedAuthorizationFilter).
 */
class RulePathMatcherEquivalenciaTest {

    private val novo = RulePathMatcher()
    private val contexto = RulePathMatcher("/api")

    /** A implementacao antiga, por regex. */
    private fun antigo(pattern: String, path: String, context: String = ""): Boolean {
        fun removeContext(p: String): String {
            val ctxRaw = context.trim()
            if (ctxRaw.isEmpty()) return p
            val ctx = "/" + ctxRaw.trim('/')
            if (p == ctx) return "/"
            return if (p.startsWith("$ctx/")) p.removePrefix(ctx).let { if (it.isEmpty()) "/" else it } else p
        }
        fun normalizePath(p: String): String {
            if (p.isEmpty()) return "/"
            val withLeading = if (p.startsWith("/")) p else "/$p"
            return removeContext(withLeading.replace(Regex("/+"), "/"))
        }
        val adjustedPattern = normalizePath(pattern)
        val adjustedPath = normalizePath(path)
        if (adjustedPattern.endsWith("/**")) return adjustedPath.startsWith(adjustedPattern.removeSuffix("/**"))
        val regexPattern = adjustedPattern
            .replace("**", "__DOUBLE_WILDCARD__")
            .replace("*", "[^/]*")
            .replace("__DOUBLE_WILDCARD__", ".*")
            .replace("/$", "(/.*)?")
        return adjustedPath.matches(Regex(regexPattern))
    }

    /** Cada simbolo do alfabeto e uma String (a lista permite ponto de codigo fora do BMP, como um emoji). */
    private fun aleatorio(random: Random, alfabeto: List<String>, maximo: Int) =
        (0 until random.nextInt(0, maximo + 1)).joinToString("") { alfabeto[random.nextInt(alfabeto.size)] }

    private fun simbolos(s: String) = s.map { it.toString() }

    @Test
    fun `da o mesmo resultado que o regex antigo em milhares de casos aleatorios`() {
        val random = Random(20260930)
        var casaram = 0
        repeat(200_000) {
            val padrao = aleatorio(random, simbolos("ab/*.-_") + "😀", 9)
            val caminho = aleatorio(random, simbolos("ab/.-_") + "😀" + "\n", 11)
            val esperado = antigo(padrao, caminho)
            assertEquals(esperado, novo.matchPath(padrao, caminho), "padrao='$padrao' caminho='$caminho'")
            if (esperado) casaram++
        }
        // o teste so vale se exercita os dois lados
        assertTrue(casaram > 5_000, "casaram=$casaram")
    }

    @Test
    fun `da o mesmo resultado com context-path configurado`() {
        val random = Random(7)
        repeat(20_000) {
            val padrao = aleatorio(random, simbolos("ab/*"), 7)
            val caminho = (if (random.nextBoolean()) "/api" else "") + aleatorio(random, simbolos("ab/"), 8)
            assertEquals(antigo(padrao, caminho, "/api"), contexto.matchPath(padrao, caminho), "padrao='$padrao' caminho='$caminho'")
        }
    }

    @Test
    fun `da o mesmo resultado nos rules yml reais para os caminhos que a aplicacao usa`() {
        val urls = listOf(
            "/sales-person/*", "/sales-person/*/business-partners", "/invoice/**/parcela/*", "/cobranca/titulos/*/*/*/historico/*",
            "/**", "/painel/**", "/pix/gerar-chave/**", "/tax/**/pdf", "/forma-pagamento/filial/*/cardcode/*", "/branch",
        )
        val caminhos = listOf(
            "/sales-person/137", "/sales-person/137/business-partners", "/invoice/cardcode/C1/parcela/2",
            "/cobranca/titulos/NF/500/1/historico/2", "/painel-vendas/v2/kpis", "/pix/gerar-chave/x/y", "/tax/1/2/pdf",
            "/forma-pagamento/filial/3/cardcode/C1", "/branch", "/branch/", "/", "/x",
        )
        for (u in urls) for (c in caminhos) assertEquals(antigo(u, c), novo.matchPath(u, c), "padrao='$u' caminho='$c'")
    }

    @Test
    fun `o ponto do padrao casa um ponto de codigo inteiro, como no regex antigo`() {
        assertEquals(antigo("/a..b", "/a😀b"), novo.matchPath("/a..b", "/a😀b"))
        assertFalse(novo.matchPath("/a..b", "/a😀b"))
        assertTrue(novo.matchPath("/a.b", "/a😀b"))
    }

    @Test
    fun `o cache de programas tem teto e continua correto depois de esvaziar`() {
        repeat(5_000) { assertFalse(novo.matchPath("/recurso-$it/*", "/outro/1")) }
        assertTrue(novo.matchPath("/recurso-1/*", "/recurso-1/2"))
    }

    @Test
    fun `caminho longo e sem chance nao custa o padrao inteiro`() {
        val caminho = "/" + "a".repeat(8_000)
        val padroes = (1..50).map { "/sales-person/*/dados-$it/mais/um/trecho/comprido/para/o/padrao/ficar/grande/" + "x".repeat(120) }
        assertTimeoutPreemptively(Duration.ofSeconds(1), {
            repeat(50) { padroes.forEach { assertFalse(novo.matchPath(it, caminho)) } }
        })
    }

    @Test
    fun `padrao que custava segundos no regex agora e imediato`() {
        val caminho = "/" + "a".repeat(1500) + "/" + "b".repeat(1500)
        listOf("/**a**/*b*x", "/**/**/**/x", "/********************x", "/*a*a*a*a*a*a*a*b").forEach { padrao ->
            assertTimeoutPreemptively(Duration.ofSeconds(1), {
                assertFalse(novo.matchPath(padrao, caminho))
            }, padrao)
        }
    }
}
