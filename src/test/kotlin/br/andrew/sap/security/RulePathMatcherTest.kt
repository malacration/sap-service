package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RulePathMatcher
import br.andrew.sap.services.security.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * O matcher e o que o filtro usa e o que o simulador da tela de regras usa. Estes casos fixam o
 * comportamento de sempre; o RoleBasedAuthorizationFilterTest continua cobrindo o filtro em si.
 */
class RulePathMatcherTest {

    private val matcher = RulePathMatcher()

    @Test
    fun `asterisco casa um unico segmento`() {
        assertTrue(matcher.matchPath("/sales-person/*", "/sales-person/137"))
        assertFalse(matcher.matchPath("/sales-person/*", "/sales-person/137/business-partners"))
        assertFalse(matcher.matchPath("/sales-person/*", "/sales-person"))
    }

    @Test
    fun `asterisco no meio nao atravessa barra`() {
        assertTrue(matcher.matchPath("/sales-person/*/business-partners", "/sales-person/137/business-partners"))
        assertFalse(matcher.matchPath("/sales-person/*/business-partners", "/sales-person/1/2/business-partners"))
    }

    @Test
    fun `duplo asterisco no fim casa o prefixo inteiro`() {
        assertTrue(matcher.matchPath("/cobranca/**", "/cobranca/titulos/NF/500/1/historico"))
        assertTrue(matcher.matchPath("/**", "/qualquer/coisa"))
    }

    @Test
    fun `duplo asterisco no meio atravessa segmentos`() {
        assertTrue(matcher.matchPath("/invoice/**/parcela/*", "/invoice/cardcode/C1/parcela/2"))
        assertFalse(matcher.matchPath("/invoice/**/parcela/*", "/invoice/cardcode/C1/outra/2"))
    }

    @Test
    fun `sufixo duplo asterisco nao tem fronteira de barra`() {
        // Aresta antiga, preservada de proposito: /painel/** tambem libera /painel-vendas.
        assertTrue(matcher.matchPath("/painel/**", "/painel-vendas/v2/kpis"))
    }

    @Test
    fun `caminho sem barra inicial e barras repetidas sao normalizados`() {
        assertTrue(matcher.matchPath("/branch", "branch"))
        assertTrue(matcher.matchPath("/branch", "//branch"))
    }

    @Test
    fun `remove o context-path configurado`() {
        val comContexto = RulePathMatcher("/api")
        assertTrue(comContexto.matchPath("/branch", "/api/branch"))
        assertTrue(comContexto.matchPath("/branch", "/branch"))
    }

    @Test
    fun `metodo ignora caixa e o curinga libera qualquer metodo`() {
        val get = Rule("/branch", listOf("get"))
        assertNotNull(matcher.autoriza(listOf(get), "GET", "/branch"))
        assertNull(matcher.autoriza(listOf(get), "POST", "/branch"))
        assertNotNull(matcher.autoriza(listOf(Rule("/branch", listOf("*"))), "DELETE", "/branch"))
    }

    @Test
    fun `devolve a regra que casou`() {
        val regras = listOf(Rule("/branch", "get"), Rule("/sales-person/*", "get"))
        assertEquals(Rule("/sales-person/*", "get"), matcher.autoriza(regras, "get", "/sales-person/137"))
    }

    @Test
    fun `corrida de asteriscos nao causa backtracking explosivo e casa o mesmo que dois`() {
        // 20 asteriscos viravam 10 ".*" seguidos: uma requisicao comum travava uma thread por segundos.
        val caminho = "/" + "a".repeat(30)
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2)) {
            assertFalse(matcher.matchPath("/********************x", caminho))
            assertTrue(matcher.matchPath("/********************x", caminho + "x"))
        }
        assertTrue(matcher.matchPath("/a/***/c", "/a/b/x/c"))
    }

    @Test
    fun `padrao com chaves e invalido para o filtro`() {
        // {id} viraria PatternSyntaxException (500) no meio de uma requisicao.
        assertNotNull(matcher.problemaDoPadrao("/business-partners/{id}"))
        assertNull(matcher.problemaDoPadrao("/business-partners/*"))
        assertNull(matcher.problemaDoPadrao("/cobranca/**"))
    }
}
