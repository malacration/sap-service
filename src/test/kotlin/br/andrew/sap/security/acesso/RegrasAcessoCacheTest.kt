package br.andrew.sap.security.acesso

import br.andrew.sap.model.acesso.AcessoRegrasVersao
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import br.andrew.sap.services.security.acesso.RegrasCodec
import br.andrew.sap.services.security.acesso.RegrasDocumento
import br.andrew.sap.services.security.acesso.SapRuleService
import br.andrew.sap.services.security.acesso.decidirAposFalhaNaGravacao
import br.andrew.sap.services.security.acesso.RegrasConflitoException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * O cache e o que segura o acesso de todo mundo, entao cada regra de seguranca dele tem um teste:
 * boot que falha em vez de abrir, versao estragada que e pulada, SAP fora do ar que nao derruba o
 * que ja estava valendo, e snapshot que so avanca.
 */
class RegrasAcessoCacheTest {

    private val repo = RepositorioEmMemoria()

    private fun doc(vararg extras: Pair<String, String>): RegrasDocumento =
        RegrasDocumento(
            perfis = linkedMapOf(
                "admin" to listOf(RegraDoc("/**", listOf("*"))),
                *extras.map { (perfil, url) -> perfil to listOf(RegraDoc(url, listOf("get"))) }.toTypedArray(),
            )
        )

    private fun json(vararg extras: Pair<String, String>) = RegrasCodec.toJson(doc(*extras))

    private fun cache() = RegrasAcessoCache(repo, 0)

    // ------------------------------------------------------------------ boot

    @Test
    fun `no boot carrega a versao mais nova`() {
        repo.inserir(1, json())
        repo.inserir(2, json("cobranca" to "/cobranca/**"))

        val snapshot = cache().carregarNoBoot()

        assertEquals(2, snapshot.versao)
        assertEquals(listOf("/cobranca/**"), SapRuleService(cache().also { it.carregarNoBoot() }).get("cobranca").map { it.url })
    }

    @Test
    fun `sem nenhuma versao o boot falha com a orientacao`() {
        val erro = assertThrows(IllegalStateException::class.java) { cache().carregarNoBoot() }
        assertTrue(erro.message!!.contains("fonte=arquivo"))
    }

    @Test
    fun `versao mais nova estragada derruba o boot em vez de voltar para uma anterior`() {
        // a mais nova pode ter revogado um acesso: carregar a anterior o devolveria em silencio
        repo.inserir(1, json("cobranca" to "/cobranca/**"))
        repo.inserir(2, "isto nao e json")

        val erro = assertThrows(IllegalStateException::class.java) { cache().carregarNoBoot() }

        assertTrue(erro.message!!.contains("NAO sera trocada por uma anterior"))
    }

    @Test
    fun `versao mais nova sem o curinga do admin tambem derruba o boot`() {
        repo.inserir(1, json())
        repo.inserir(2, json().replace("\"/**\"", "\"/so-isso\""))

        assertThrows(IllegalStateException::class.java) { cache().carregarNoBoot() }
    }

    @Test
    fun `se nenhuma versao presta o boot falha em vez de abrir ou fechar tudo`() {
        repo.inserir(1, "lixo")
        repo.inserir(2, "{\"perfis\":{}}")

        assertThrows(IllegalStateException::class.java) { cache().carregarNoBoot() }
    }

    @Test
    fun `sap fora do ar no boot derruba o boot, nao cai para o arquivo`() {
        repo.inserir(1, json())
        repo.falhaNaLeitura = { RuntimeException("connection refused") }

        assertThrows(RuntimeException::class.java) { cache().carregarNoBoot() }
    }

    @Test
    fun `tabela recem criada responde is invalid e o boot tenta de novo`() {
        repo.inserir(1, json())
        var falhas = 2
        repo.falhaNaLeitura = { if (falhas-- > 0) RuntimeException("Table 'ACESSO_REGRAS' is invalid") else null }

        assertEquals(1, cache().carregarNoBoot().versao)
    }

    @Test
    fun `erro que nao e de propagacao nao e repetido`() {
        repo.inserir(1, json())
        repo.falhaNaLeitura = { RuntimeException("Invalid session") }

        assertThrows(RuntimeException::class.java) { cache().carregarNoBoot() }
        assertEquals(1, repo.leituras)
    }

    @Test
    fun `antes de carregar consultar as regras e erro, nunca um mapa vazio`() {
        assertThrows(IllegalStateException::class.java) { SapRuleService(cache()).get("admin") }
    }

    // ------------------------------------------------------------------ em execucao

    @Test
    fun `o refresh so troca quando ha versao maior`() {
        repo.inserir(1, json())
        val c = cache().also { it.carregarNoBoot() }
        val antes = c.atual()

        c.conferir()
        assertSame(antes, c.atual())
        assertEquals(0, repo.buscas - 1) // so a busca do boot

        repo.inserir(2, json("cobranca" to "/cobranca/**"))
        c.conferir()

        assertEquals(2, c.atual().versao)
        assertEquals(listOf("/cobranca/**"), c.atual().regras.getValue("cobranca").map { it.url })
    }

    @Test
    fun `sap fora do ar no refresh mantem o ultimo snapshot e nao lanca`() {
        repo.inserir(1, json("cobranca" to "/cobranca/**"))
        val c = cache().also { it.carregarNoBoot() }
        repo.inserir(2, json())
        repo.falhaNaLeitura = { RuntimeException("timeout") }

        c.conferir()

        assertEquals(1, c.atual().versao)
        assertTrue(c.atual().regras.containsKey("cobranca"))
    }

    @Test
    fun `versao nova estragada no refresh e ignorada e nao e rebuscada a cada rodada`() {
        repo.inserir(1, json())
        val c = cache().also { it.carregarNoBoot() }
        repo.inserir(2, "lixo")

        c.conferir()
        val buscasDepoisDaPrimeira = repo.buscas
        c.conferir()
        c.conferir()

        assertEquals(1, c.atual().versao)
        assertEquals(buscasDepoisDaPrimeira, repo.buscas)

        repo.inserir(3, json("cobranca" to "/cobranca/**"))
        c.conferir()
        assertEquals(3, c.atual().versao)
    }

    @Test
    fun `o snapshot so avanca`() {
        repo.inserir(1, json())
        val c = cache().also { it.carregarNoBoot() }

        assertTrue(c.aplicar(5, doc("cobranca" to "/cobranca/**")))
        assertFalse(c.aplicar(4, doc()))
        assertFalse(c.aplicar(5, doc()))

        assertEquals(5, c.atual().versao)
        assertTrue(c.atual().regras.containsKey("cobranca"))
    }

    // ------------------------------------------------------------------ decisao apos falha no POST

    private val versao = AcessoRegrasVersao(Code = "000007", U_IdEscrita = "meu-id")
    private val erroOriginal = RuntimeException("timeout no POST")

    @Test
    fun `falha no POST com a linha ausente relanca o erro real`() {
        val lancado = assertThrows(RuntimeException::class.java) { decidirAposFalhaNaGravacao(erroOriginal, versao, null) }
        assertSame(erroOriginal, lancado)
    }

    @Test
    fun `falha no POST com a linha sendo nossa e sucesso (so a resposta se perdeu)`() {
        decidirAposFalhaNaGravacao(erroOriginal, versao, "meu-id")
    }

    @Test
    fun `falha no POST com a linha de outra gravacao e conflito`() {
        assertThrows(RegrasConflitoException::class.java) { decidirAposFalhaNaGravacao(erroOriginal, versao, "id-de-outro") }
        assertThrows(RegrasConflitoException::class.java) { decidirAposFalhaNaGravacao(erroOriginal, versao, "") }
    }
}
