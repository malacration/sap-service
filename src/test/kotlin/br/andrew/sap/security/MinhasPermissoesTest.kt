package br.andrew.sap.security

import br.andrew.sap.controllers.authentication.MinhasPermissoesController
import br.andrew.sap.controllers.authentication.PermissaoConsulta
import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.security.acesso.RepositorioEmMemoria
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoFonteConfiguration
import br.andrew.sap.services.security.acesso.RegrasAcessoRepositorio
import br.andrew.sap.services.security.acesso.RegrasCodec
import br.andrew.sap.services.security.acesso.RegrasDocumento
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.function.Supplier

/**
 * O menu do front pergunta ao POST /acesso/minhas-permissoes quais telas mostrar. Tem que responder exatamente o
 * que o filtro faria (senao aparece tela que da 403, ou some tela que funciona) e tem que poder ser chamado por
 * qualquer usuario autenticado, inclusive perfil sem regra nenhuma.
 */
class MinhasPermissoesTest {

    private fun usuario(vararg roles: String) =
        User("1", "Fulano", UserOriginEnum.EmployeesInfo, "", "", "", listOf(), roles.toList())

    private val semRole = usuario()
    private val parceiro = usuario("business_partner")
    private val vendedor = usuario("vendedor")
    private val vendedorAdmin = usuario("vendedor_admin")
    private val admin = usuario("admin")

    private val filtro = RoleBasedAuthorizationFilter(MockRuleService(), "")
    private val controller = MinhasPermissoesController(MockRuleService(), "")
    private val mvc: MockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    // ------------------------------------------------------------------ filtro

    @Test
    fun `filtro libera o endpoint para quem nao tem regra nenhuma`() {
        assertTrue(filtro.isAuthorized("/acesso/minhas-permissoes", "POST", semRole))
        assertTrue(filtro.isAuthorized("/acesso/minhas-permissoes", "post", parceiro))
    }

    @Test
    fun `a liberacao e so deste caminho e deste metodo`() {
        assertFalse(filtro.isAuthorized("/acesso/minhas-permissoes", "GET", semRole))
        assertFalse(filtro.isAuthorized("/acesso/minhas-permissoes", "DELETE", semRole))
        assertFalse(filtro.isAuthorized("/acesso/minhas-permissoes/x", "POST", semRole))
        assertFalse(filtro.isAuthorized("/acesso/minhas-permissoes-x", "POST", semRole))
        assertFalse(filtro.isAuthorized("/acesso/regras", "POST", semRole))
        assertFalse(filtro.isAuthorized("/acesso/regras/simular", "POST", semRole))
        assertFalse(filtro.isAuthorized("/acesso", "POST", semRole))
    }

    @Test
    fun `com context path a liberacao segue o mesmo caminho`() {
        val comContexto = RoleBasedAuthorizationFilter(MockRuleService(), "/v7")
        assertTrue(comContexto.isAuthorized("/v7/acesso/minhas-permissoes", "POST", semRole))
        assertFalse(comContexto.isAuthorized("/v7/acesso/regras", "POST", semRole))
    }

    // ------------------------------------------------------------------ resposta

    @Test
    fun `responde item a item na ordem pedida, devolvendo o que foi perguntado`() {
        val resposta = controller.minhasPermissoes(vendedor, listOf(
            PermissaoConsulta("get", "/clientes/1"),
            PermissaoConsulta("post", "/clientes/1"),
            PermissaoConsulta("GET", "https://cadastro-back.exemplo.com/clientes/2?aba=x"),
            PermissaoConsulta("get", "clientes/3"),
        ))
        assertEquals(listOf(true, false, true, true), resposta.map { it.permitido })
        assertEquals("GET", resposta[2].metodo)
        assertEquals("https://cadastro-back.exemplo.com/clientes/2?aba=x", resposta[2].caminho)
    }

    @Test
    fun `perfis somam e admin pode tudo`() {
        val dois = usuario("vendedor", "pix")
        val resposta = controller.minhasPermissoes(dois, listOf(PermissaoConsulta("get", "/clientes/1"), PermissaoConsulta("get", "/pix")))
        assertEquals(listOf(true, true), resposta.map { it.permitido })
        assertTrue(controller.minhasPermissoes(admin, listOf(PermissaoConsulta("delete", "/qualquer/coisa"))).single().permitido)
    }

    @Test
    fun `resposta igual ao filtro para varios perfis e caminhos`() {
        val usuarios = listOf(semRole, parceiro, vendedor, vendedorAdmin, admin, usuario("pix"), usuario("vendedor", "pix"))
        val caminhos = listOf(
            "/clientes/666", "/clientes/666/jose", "/clientes", "/vendedor", "/vendedor/1/x", "/vendedores",
            "/pix", "/pix/transaction/abc", "/invoice/10/create-pix", "/invoice/pix", "/invoice",
            "/acesso/minhas-permissoes", "/acesso/regras",
        )
        val metodos = listOf("get", "post", "put", "patch", "delete")
        for (u in usuarios) {
            val consultas = caminhos.flatMap { c -> metodos.map { PermissaoConsulta(it, c) } }
            val resposta = controller.minhasPermissoes(u, consultas)
            consultas.zip(resposta).forEach { (c, r) ->
                assertEquals(filtro.isAuthorized(c.caminho, c.metodo, u), r.permitido, "${u.roles} ${c.metodo} ${c.caminho}")
            }
        }
    }

    @Test
    fun `com context path a resposta e a da URI que a chamada real teria`() {
        val comContexto = MinhasPermissoesController(MockRuleService(), "/v7")
        val filtroComContexto = RoleBasedAuthorizationFilter(MockRuleService(), "/v7/")
        val caminhos = listOf("/clientes/1", "/v7/clientes/1", "clientes/1", "/v7", "/acesso/minhas-permissoes", "/v7/acesso/minhas-permissoes")
        val resposta = comContexto.minhasPermissoes(vendedor, caminhos.map { PermissaoConsulta("get", it) } +
            caminhos.map { PermissaoConsulta("post", it) })
        resposta.forEach { r ->
            val uri = "/v7" + (if (r.caminho.startsWith("/")) r.caminho else "/${r.caminho}")
            assertEquals(filtroComContexto.isAuthorized(uri, r.metodo, vendedor), r.permitido, "${r.metodo} ${r.caminho}")
        }
        assertEquals(listOf(true, false), resposta.take(2).map { it.permitido })

        val urlCompleta = comContexto.minhasPermissoes(vendedor, listOf(
            PermissaoConsulta("get", "https://back.exemplo.com/v7/clientes/1"),
            PermissaoConsulta("get", "https://back.exemplo.com/v7/v7/clientes/1"),
            PermissaoConsulta("get", "/v7/clientes/1?volta=https://x"),
            PermissaoConsulta("get", "/clientes/1?volta=https://x"),
        ))
        assertEquals(filtroComContexto.isAuthorized("/v7/clientes/1", "get", vendedor), urlCompleta[0].permitido)
        assertEquals(filtroComContexto.isAuthorized("/v7/v7/clientes/1", "get", vendedor), urlCompleta[2].permitido)
        assertEquals(listOf(true, false, false, true), urlCompleta.map { it.permitido })
    }

    // ------------------------------------------------------------------ HTTP

    @Test
    fun `http devolve a lista no formato que o front le`() {
        mvc.perform(post("/acesso/minhas-permissoes").principal(vendedorAdmin).contentType(MediaType.APPLICATION_JSON)
            .content("""[{"metodo":"get","caminho":"/clientes/1"},{"metodo":"patch","caminho":"/clientes/1"}]"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].metodo").value("get"))
            .andExpect(jsonPath("$[0].caminho").value("/clientes/1"))
            .andExpect(jsonPath("$[0].permitido").value(true))
            .andExpect(jsonPath("$[1].permitido").value(false))
    }

    @Test
    fun `lista vazia responde lista vazia`() {
        mvc.perform(post("/acesso/minhas-permissoes").principal(semRole).contentType(MediaType.APPLICATION_JSON).content("[]"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `mais de cem consultas responde 400`() {
        val muitas = (1..101).joinToString(",", "[", "]") { """{"metodo":"get","caminho":"/x/$it"}""" }
        mvc.perform(post("/acesso/minhas-permissoes").principal(vendedor).contentType(MediaType.APPLICATION_JSON).content(muitas))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.erro").value("consulta_invalida"))
            .andExpect(jsonPath("$.mensagem").value(containsString("100")))

        val cem = (1..100).joinToString(",", "[", "]") { """{"metodo":"get","caminho":"/x/$it"}""" }
        mvc.perform(post("/acesso/minhas-permissoes").principal(vendedor).contentType(MediaType.APPLICATION_JSON).content(cem))
            .andExpect(status().isOk)
    }

    @Test
    fun `metodo fora da lista, curinga, caminho vazio ou longo demais respondem 400`() {
        listOf(
            """[{"metodo":"*","caminho":"/clientes/1"}]""",
            """[{"metodo":"trace","caminho":"/clientes/1"}]""",
            """[{"caminho":"/clientes/1"}]""",
            """[{"metodo":"get","caminho":"  "}]""",
            """[{"metodo":"get"}]""",
            """[{"metodo":"get","caminho":"/${"a".repeat(300)}"}]""",
        ).forEach { corpo ->
            mvc.perform(post("/acesso/minhas-permissoes").principal(admin).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.erro").value("consulta_invalida"))
        }
    }

    @Test
    fun `json invalido responde 400 e nao 500`() {
        listOf("{", """{"metodo":"get","caminho":"/x"}""", """[{"metodo":["get"],"caminho":"/x"}]""", "[null]",
            """[{"metodo":"get","caminho":"/x"},null]""", """[{"metodo":null,"caminho":"/x"}]""").forEach { corpo ->
            mvc.perform(post("/acesso/minhas-permissoes").principal(vendedor).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.erro").value("consulta_invalida"))
        }
    }

    // ------------------------------------------------------------------ fonte das regras

    private val repo = RepositorioEmMemoria().also {
        it.inserir(1, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf(
            "admin" to listOf(RegraDoc("/**", listOf("*"))),
            "conferente" to listOf(RegraDoc("/carregamento/**", listOf("get"))),
        ))))
    }

    private fun runner() = ApplicationContextRunner()
        .withUserConfiguration(RegrasArquivoService::class.java, RegrasAcessoFonteConfiguration::class.java, MinhasPermissoesController::class.java)
        .withBean(RegrasAcessoRepositorio::class.java, Supplier { repo })

    private val carregamento = listOf(PermissaoConsulta("get", "/carregamento"), PermissaoConsulta("get", "/cobranca/titulos"))

    @Test
    fun `com fonte sap responde pelo cadastro do SAP`() {
        runner().withPropertyValues("acesso.regras.fonte=sap").run { ctx ->
            val resposta = ctx.getBean(MinhasPermissoesController::class.java).minhasPermissoes(usuario("conferente"), carregamento)
            assertEquals(listOf(true, false), resposta.map { it.permitido })
        }
    }

    @Test
    fun `com fonte arquivo responde pelo arquivo, sem o cadastro do SAP`() {
        runner().withPropertyValues("acesso.regras.fonte=arquivo").run { ctx ->
            val controller = ctx.getBean(MinhasPermissoesController::class.java)
            assertEquals(listOf(false, false), controller.minhasPermissoes(usuario("conferente"), carregamento).map { it.permitido })
            assertEquals(listOf(false, true), controller.minhasPermissoes(usuario("cobranca"), carregamento).map { it.permitido })
        }
        runner().run { ctx ->
            assertEquals(listOf(false, true), ctx.getBean(MinhasPermissoesController::class.java)
                .minhasPermissoes(usuario("cobranca"), carregamento).map { it.permitido })
        }
    }
}
