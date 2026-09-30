package br.andrew.sap.security.acesso

import br.andrew.sap.controllers.authentication.RegrasAcessoController
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import br.andrew.sap.services.security.acesso.RegrasAcessoService
import br.andrew.sap.services.security.interfaces.RuleService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * A camada HTTP de verdade: o Jackson lendo/escrevendo as classes Kotlin, o corpo em texto do YAML e
 * os handlers de erro do controller (que precisam devolver 403/400/409 e nao o 500 do handler
 * global). O formato dos JSONs aqui e o contrato que o front (regras-acesso.service.ts) consome.
 */
class RegrasAcessoHttpTest {

    private val repo = RepositorioEmMemoria()
    private val yaml = "roles:\n  admin:\n    - url: \"/**\"\n      actions: [\"*\"]\n  cobranca:\n    - url: \"/cobranca/**\"\n      actions: [\"get\"]\n"

    private val admin = User("60", "Fulano", UserOriginEnum.SalePerson, "fulano@empresa.com", "", "", listOf(), listOf("admin"))
    private val vendedorAdmin = User("61", "Ciclano", UserOriginEnum.SalePerson, "ciclano", "", "", listOf(), listOf("vendedor_admin"))

    private val mvc: MockMvc = run {
        val loader = object : ResourceLoader by DefaultResourceLoader() {
            override fun getResource(location: String): Resource = ByteArrayResource(yaml.toByteArray())
        }
        val arquivo = RegrasArquivoService(loader, "memoria:rules.yml")
        val bf = DefaultListableBeanFactory().apply { registerSingleton("regra", arquivo) }
        val service = RegrasAcessoService(
            repo, arquivo, bf.getBeanProvider(RegrasAcessoCache::class.java), bf.getBeanProvider(RuleService::class.java), "",
        ).also { it.semearSeVazio() }
        MockMvcBuilders.standaloneSetup(RegrasAcessoController(service, RequestMappingHandlerMapping())).build()
    }

    @Test
    fun `GET devolve o estado no formato que o front le`() {
        mvc.perform(get("/acesso/regras").principal(admin))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.versao").value(1))
            .andExpect(jsonPath("$.fonteAtiva").value("arquivo"))
            .andExpect(jsonPath("$.documento.perfis.cobranca[0].url").value("/cobranca/**"))
            .andExpect(jsonPath("$.documento.perfis.cobranca[0].actions[0]").value("get"))
            .andExpect(jsonPath("$.resumo.origem").value("SEED"))
            .andExpect(jsonPath("$.perfisProtegidos[?(@ == 'cobranca')]").exists())
            .andExpect(jsonPath("$.arquivoLocal").value("memoria:rules.yml"))
    }

    @Test
    fun `PUT do perfil le o corpo json e grava a versao seguinte`() {
        val corpo = """{"regras":[{"url":"/cobranca/**","actions":["get"]},{"url":"/sales-person/*","actions":["get"],"comentario":"nome"}],"baseVersao":1,"comentario":"libera o nome"}"""

        mvc.perform(put("/acesso/regras/perfis/cobranca").principal(admin).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.versao").value(2))
            .andExpect(jsonPath("$.documento.perfis.cobranca[1].url").value("/sales-person/*"))
            .andExpect(jsonPath("$.documento.perfis.cobranca[1].comentario").value("nome"))
            .andExpect(jsonPath("$.resumo.comentario").value("libera o nome"))
    }

    @Test
    fun `PUT com base velha responde 409 com a mensagem para a tela`() {
        val corpo = """{"regras":[{"url":"/a","actions":["get"]}],"baseVersao":9}"""

        mvc.perform(put("/acesso/regras/perfis/cobranca").principal(admin).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.erro").value("conflito"))
            .andExpect(jsonPath("$.mensagem").value(containsString("Recarregue")))
    }

    @Test
    fun `regra invalida responde 400 com a lista de erros`() {
        val corpo = """{"regras":[{"url":"sem-barra","actions":["trace"]}],"baseVersao":1}"""

        mvc.perform(put("/acesso/regras/perfis/novo").principal(admin).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.erro").value("validacao"))
            .andExpect(jsonPath("$.erros.length()").value(2))
            .andExpect(jsonPath("$.mensagem").value(containsString("sem-barra")))
    }

    @Test
    fun `quem nao e admin toma 403 com json, nao 500`() {
        mvc.perform(get("/acesso/regras").principal(vendedorAdmin))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.erro").value("acesso_negado"))

        mvc.perform(put("/acesso/regras/perfis/admin").principal(vendedorAdmin).contentType(MediaType.APPLICATION_JSON)
            .content("""{"regras":[{"url":"/**","actions":["*"]}],"baseVersao":1}"""))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `excluir perfil do sistema responde 400 e perfil inexistente 404`() {
        mvc.perform(delete("/acesso/regras/perfis/cobranca").principal(admin).param("baseVersao", "1"))
            .andExpect(status().isBadRequest)
        mvc.perform(delete("/acesso/regras/perfis/fantasma").principal(admin).param("baseVersao", "1"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `importar aceita o yaml como texto, previa por padrao e grava com simular=false`() {
        val novo = "roles:\n  conferente:\n    - url: \"/branch\"\n      actions: [\"get\"]\n"

        mvc.perform(post("/acesso/regras/importar").principal(admin).contentType(MediaType.TEXT_PLAIN).content(novo))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.modo").value("mesclar"))
            .andExpect(jsonPath("$.diff[0].perfil").value("conferente"))
            .andExpect(jsonPath("$.diff[0].tipo").value("NOVO"))
            .andExpect(jsonPath("$.erros.length()").value(0))
        mvc.perform(get("/acesso/regras").principal(admin)).andExpect(jsonPath("$.versao").value(1))

        mvc.perform(post("/acesso/regras/importar").principal(admin).contentType(MediaType.TEXT_PLAIN).content(novo)
            .param("simular", "false").param("baseVersao", "1").param("comentario", "traz o conferente"))
            .andExpect(status().isOk)
        mvc.perform(get("/acesso/regras").principal(admin))
            .andExpect(jsonPath("$.versao").value(2))
            .andExpect(jsonPath("$.documento.perfis.conferente[0].url").value("/branch"))
    }

    @Test
    fun `previa de yaml quebrado e 200 com o erro dentro`() {
        mvc.perform(post("/acesso/regras/importar").principal(admin).contentType(MediaType.TEXT_PLAIN).content("roles: ["))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.erros[0]").value(containsString("YAML invalido")))
    }

    @Test
    fun `exportar e arquivo devolvem yaml em texto puro`() {
        mvc.perform(get("/acesso/regras/exportar").principal(admin))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string(containsString("roles:")))
        mvc.perform(get("/acesso/regras/arquivo").principal(admin))
            .andExpect(status().isOk)
            .andExpect(content().string(yaml))
    }

    @Test
    fun `simular le o corpo e responde o veredito com a regra`() {
        val corpo = """{"perfis":["cobranca"],"metodo":"get","caminho":"https://x.com.br/cobranca/titulos?p=1"}"""

        mvc.perform(post("/acesso/regras/simular").principal(admin).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.autorizado").value(true))
            .andExpect(jsonPath("$.regra.perfil").value("cobranca"))
            .andExpect(jsonPath("$.caminho").value("/cobranca/titulos"))
    }

    @Test
    fun `versoes e restaurar`() {
        mvc.perform(put("/acesso/regras/perfis/cobranca").principal(admin).contentType(MediaType.APPLICATION_JSON)
            .content("""{"regras":[{"url":"/a","actions":["get"]}],"baseVersao":1}"""))
            .andExpect(status().isOk)

        mvc.perform(get("/acesso/regras/versoes").principal(admin))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].versao").value(2))
        mvc.perform(get("/acesso/regras/versoes/2").principal(admin))
            .andExpect(jsonPath("$.diff[0].perfil").value("cobranca"))
            .andExpect(jsonPath("$.diff[0].tipo").value("ALTERADO"))
        mvc.perform(post("/acesso/regras/versoes/1/restaurar").principal(admin).contentType(MediaType.APPLICATION_JSON)
            .content("""{"baseVersao":2}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.versao").value(3))
            .andExpect(jsonPath("$.resumo.origem").value("RESTAURACAO"))
    }
}
