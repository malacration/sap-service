package br.andrew.sap.security.acesso

import br.andrew.sap.controllers.authentication.RegrasAcessoController
import br.andrew.sap.controllers.authentication.RestaurarRequest
import br.andrew.sap.controllers.authentication.SalvarPerfilRequest
import br.andrew.sap.controllers.authentication.SimularRequest
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import br.andrew.sap.services.security.acesso.RegrasAcessoNegadoException
import br.andrew.sap.services.security.acesso.RegrasAcessoService
import br.andrew.sap.services.security.acesso.RegrasConflitoException
import br.andrew.sap.services.security.acesso.RegrasNaoEncontradasException
import br.andrew.sap.services.security.acesso.RegrasValidacaoException
import br.andrew.sap.services.security.interfaces.RuleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.support.StaticApplicationContext
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * O controller e a porta de entrada da gestao das regras: quem nao e admin nao passa em rota
 * nenhuma, os erros tem status proprios (nao o 500 + Telegram do handler global) e o catalogo de
 * endpoints sai no formato das regras.
 */
class RegrasAcessoControllerTest {

    private val repo = RepositorioEmMemoria()
    private val yaml = "roles:\n  admin:\n    - url: \"/**\"\n      actions: [\"*\"]\n  cobranca:\n    - url: \"/cobranca/**\"\n      actions: [\"get\"]\n"

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    private val admin = usuario("admin")

    @RestController
    @RequestMapping("dummy")
    class DummyController {
        @GetMapping("/{id}") fun um(@PathVariable id: Int) = id
        @GetMapping("/{id}/itens/{item}") fun item(@PathVariable id: Int, @PathVariable item: Int) = id
        @PostMapping("/search") fun buscar(): String = ""
    }

    private fun mapeamentoDoDummy(): RequestMappingHandlerMapping {
        val ctx = StaticApplicationContext().apply {
            registerSingleton("dummy", DummyController::class.java)
            refresh()
        }
        return RequestMappingHandlerMapping().apply {
            setApplicationContext(ctx)
            afterPropertiesSet()
        }
    }

    private fun controller(mapeamento: RequestMappingHandlerMapping = RequestMappingHandlerMapping()): RegrasAcessoController {
        val loader = object : ResourceLoader by DefaultResourceLoader() {
            override fun getResource(location: String): Resource = ByteArrayResource(yaml.toByteArray())
        }
        val arquivo = RegrasArquivoService(loader, "memoria:rules.yml")
        val bf = DefaultListableBeanFactory().apply { registerSingleton("regra", arquivo) }
        val service = RegrasAcessoService(
            repo, arquivo, bf.getBeanProvider(RegrasAcessoCache::class.java), bf.getBeanProvider(RuleService::class.java), "",
            bf.getBeanProvider(br.andrew.sap.infrastructure.security.keycloak.KeycloakRolesGateway::class.java), false,
        ).also { it.semearSeVazio() }
        return RegrasAcessoController(service, mapeamento)
    }

    // ------------------------------------------------------------------ so admin

    @Test
    fun `quem nao e admin nao passa em nenhuma rota, mesmo com perfil forte`() {
        val c = controller()
        listOf(usuario(), usuario("vendedor_admin"), usuario("cobranca", "pix_admin", "liberacao_trava")).forEach { quem ->
            assertThrows(RegrasAcessoNegadoException::class.java) { c.atual(quem) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.versoes(quem) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.versao(quem, 1) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.exportar(quem, null) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.arquivo(quem) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.endpoints(quem) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.cobertura(quem, "/a") }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.simular(quem, SimularRequest(listOf("admin"), "get", "/a")) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.importar(quem, yaml, "mesclar", true, null, null) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.restaurar(quem, 1, RestaurarRequest(1)) }
            assertThrows(RegrasAcessoNegadoException::class.java) { c.removerPerfil(quem, "cobranca", 1, null) }
            assertThrows(RegrasAcessoNegadoException::class.java) {
                c.salvarPerfil(quem, "admin", SalvarPerfilRequest(listOf(RegraDoc("/**", listOf("*"))), 1))
            }
        }
        // nada foi gravado
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `principal que nao e um User tambem e negado`() {
        val c = controller()
        val anonimo = org.springframework.security.authentication.TestingAuthenticationToken("x", "y", "admin")
        assertThrows(RegrasAcessoNegadoException::class.java) { c.atual(anonimo) }
    }

    @Test
    fun `admin le e grava`() {
        val c = controller()
        assertEquals(1, c.atual(admin).versao)

        val depois = c.salvarPerfil(admin, "cobranca", SalvarPerfilRequest(listOf(RegraDoc("/cobranca/**", listOf("get")), RegraDoc("/sales-person/*", listOf("get"))), 1, "nome do vendedor"))

        assertEquals(2, depois.versao)
        assertTrue(c.exportar(admin, null).contains("/sales-person/*"))
        assertTrue(c.arquivo(admin).contains("cobranca"))
        assertTrue(c.simular(admin, SimularRequest(listOf("cobranca"), "get", "/sales-person/137")).autorizado)
    }

    // ------------------------------------------------------------------ erros com status proprio

    @Test
    fun `cada erro tem seu status e o corpo que o front le`() {
        val c = controller()

        val negado = c.onNegado(RegrasAcessoNegadoException())
        assertEquals(HttpStatus.FORBIDDEN, negado.statusCode)
        assertEquals("Você não tem permissão para acessar este recurso.", negado.body!!["mensagem"])

        val validacao = c.onValidacao(RegrasValidacaoException(listOf("erro um", "erro dois"), listOf("aviso")))
        assertEquals(HttpStatus.BAD_REQUEST, validacao.statusCode)
        assertEquals("erro um; erro dois", validacao.body!!["mensagem"])
        assertEquals(listOf("erro um", "erro dois"), validacao.body!!["erros"])
        assertEquals(listOf("aviso"), validacao.body!!["avisos"])

        val conflito = c.onConflito(RegrasConflitoException("mudou"))
        assertEquals(HttpStatus.CONFLICT, conflito.statusCode)
        assertEquals("mudou", conflito.body!!["mensagem"])

        assertEquals(HttpStatus.NOT_FOUND, c.onNaoEncontrado(RegrasNaoEncontradasException("nao ha")).statusCode)
    }

    // ------------------------------------------------------------------ catalogo de endpoints

    @Test
    fun `o catalogo de endpoints troca variavel de caminho por asterisco e traz o metodo`() {
        val c = controller(mapeamentoDoDummy())

        val urls = c.endpoints(admin).map { it.url to it.metodo }

        assertTrue(urls.contains("/dummy/*" to "get"), "$urls")
        assertTrue(urls.contains("/dummy/*/itens/*" to "get"), "$urls")
        assertTrue(urls.contains("/dummy/search" to "post"), "$urls")
        assertTrue(urls.none { it.first.contains("{") })
    }

    @Test
    fun `a cobertura de uma regra usa o catalogo real`() {
        val c = controller(mapeamentoDoDummy())

        // o asterisco alcanca qualquer segmento unico, inclusive o literal "search"
        assertEquals(listOf("/dummy/*", "/dummy/search"), c.cobertura(admin, "/dummy/{id}").map { it.url })
        assertEquals(3, c.cobertura(admin, "/dummy/**").size)
    }
}
