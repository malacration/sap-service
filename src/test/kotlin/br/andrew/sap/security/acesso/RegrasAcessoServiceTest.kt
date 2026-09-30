package br.andrew.sap.security.acesso

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import br.andrew.sap.services.security.acesso.RegrasAcessoService
import br.andrew.sap.services.security.acesso.RegrasCodec
import br.andrew.sap.services.security.acesso.RegrasDocumento
import br.andrew.sap.services.security.acesso.RegrasConflitoException
import br.andrew.sap.services.security.acesso.RegrasNaoEncontradasException
import br.andrew.sap.services.security.acesso.RegrasValidacaoException
import br.andrew.sap.services.security.acesso.RegrasYaml
import br.andrew.sap.services.security.acesso.SapRuleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * A gestao das regras contra um repositorio em memoria (sem SAP). A base e o rules.yml do swarm como
 * estava antes do ajuste de sales-person por id: e o estado real que gerou o 403.
 */
class RegrasAcessoServiceTest {

    private val yamlSwarm = RegrasAcessoServiceTest::class.java
        .getResource("/fixtures/rules-swarm-antes-sales-person.yml")!!.readText()

    private val repo = RepositorioEmMemoria()
    private val admin = User("60", "Fulano de Tal", UserOriginEnum.SalePerson, "fulano@empresa.com", "", "", listOf(), listOf("admin"))
    private val relogio = Clock.fixed(Instant.parse("2026-09-29T17:45:10Z"), ZoneId.of("UTC"))

    private fun arquivoCom(yaml: String): RegrasArquivoService {
        val loader = object : ResourceLoader by DefaultResourceLoader() {
            override fun getResource(location: String): Resource = ByteArrayResource(yaml.toByteArray())
        }
        return RegrasArquivoService(loader, "memoria:rules.yml")
    }

    /** Sem cache = fonte arquivo. Com cache = fonte sap (o filtro receberia o SapRuleService). */
    private fun servico(yaml: String = yamlSwarm, cache: RegrasAcessoCache? = null): RegrasAcessoService {
        val bf = DefaultListableBeanFactory()
        val arquivo = arquivoCom(yaml)
        if (cache != null) {
            bf.registerSingleton("cache", cache)
            bf.registerSingleton("regra", SapRuleService(cache))
        } else {
            bf.registerSingleton("regra", arquivo)
        }
        return RegrasAcessoService(
            repo, arquivo,
            bf.getBeanProvider(RegrasAcessoCache::class.java),
            bf.getBeanProvider(br.andrew.sap.services.security.interfaces.RuleService::class.java),
            "",
        ).also { it.relogio = relogio }
    }

    private fun semeado(cache: RegrasAcessoCache? = null) = servico(cache = cache).also { it.semearSeVazio() }

    private fun regra(url: String, vararg acoes: String) = RegraDoc(url, acoes.toList())

    private fun regrasDe(s: RegrasAcessoService, perfil: String) = s.atual().documento.perfis.getValue(perfil)

    // ------------------------------------------------------------------ seed

    @Test
    fun `o seed cria a versao 1 a partir do arquivo e nao se repete`() {
        val s = servico()
        s.semearSeVazio()

        assertEquals(1, repo.linhas.size)
        val v1 = repo.linhas.getValue(1)
        assertEquals("000001", v1.Code)
        assertEquals("SEED", v1.U_Origem)
        assertEquals("sistema", v1.U_Usuario)
        assertEquals(
            RegrasYaml.importar(yamlSwarm).perfis.keys.toList(),
            RegrasCodec.fromJson(v1.U_Documento!!).perfis.keys.toList(),
        )

        s.semearSeVazio()
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `arquivo invalido no seed nao grava nada e nao derruba o boot`() {
        servico(yaml = "roles:\n  x:\n    - url: \"/a\"\n      actions: [\"get\"]\n").semearSeVazio()
        assertTrue(repo.linhas.isEmpty())
    }

    @Test
    fun `sem nenhuma versao a leitura avisa em vez de quebrar`() {
        assertThrows(RegrasNaoEncontradasException::class.java) { servico().atual() }
    }

    // ------------------------------------------------------------------ gravar

    @Test
    fun `salvar um perfil cria a versao seguinte com autor, data e resumo`() {
        val s = semeado()
        val cobranca = regrasDe(s, "cobranca")

        val estado = s.salvarPerfil(admin, "cobranca", cobranca + regra("/sales-person/*", "get"), 1, "  libera o nome do vendedor  ")

        assertEquals(2, estado.versao)
        val v2 = repo.linhas.getValue(2)
        assertEquals("TELA", v2.U_Origem)
        assertEquals("fulano@empresa.com", v2.U_Usuario)
        assertEquals("60", v2.U_UsuarioId)
        assertEquals("2026-09-29", v2.U_Data)
        assertEquals("17:45:10", v2.U_Hora)
        assertEquals("libera o nome do vendedor", v2.U_Comentario)
        assertEquals("cobranca(+1)", v2.U_Resumo)
        assertNotNull(v2.U_IdEscrita)
        assertTrue(regrasDe(s, "cobranca").any { it.url == "/sales-person/*" })
        // a versao 1 continua intacta
        assertFalse(RegrasCodec.fromJson(repo.linhas.getValue(1).U_Documento!!).perfis.getValue("cobranca").any { it.url == "/sales-person/*" })
    }

    @Test
    fun `base velha e conflito e nao grava`() {
        val s = semeado()
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/a", "get"), 1, null)

        val erro = assertThrows(RegrasConflitoException::class.java) {
            s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/b", "get"), 1, null)
        }
        assertTrue(erro.message!!.contains("versão 2"))
        assertEquals(2, repo.linhas.size)
    }

    @Test
    fun `nome de perfil invalido, reservado ou regra ruim sao recusados sem gravar`() {
        val s = semeado()
        listOf("Com Espaco", "filial-3-matriz", "default-roles-rovema").forEach {
            assertThrows(RegrasValidacaoException::class.java) { s.salvarPerfil(admin, it, listOf(regra("/a", "get")), 1, null) }
        }
        assertThrows(RegrasValidacaoException::class.java) { s.salvarPerfil(admin, "novo", listOf(regra("sem-barra", "get")), 1, null) }
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `salvar sem mudar nada nao cria versao`() {
        val s = semeado()
        val erro = assertThrows(RegrasValidacaoException::class.java) {
            s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca"), 1, null)
        }
        assertTrue(erro.erros.single().contains("Nada mudou"))
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `nao deixa o admin perder o curinga`() {
        val s = semeado()
        val erro = assertThrows(RegrasValidacaoException::class.java) {
            s.salvarPerfil(admin, "admin", listOf(regra("/branch", "get")), 1, null)
        }
        assertTrue(erro.erros.any { it.contains("admin") })
    }

    @Test
    fun `perfil novo pode ser criado e depois excluido`() {
        val s = semeado()
        s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)
        assertEquals("conferente(novo +1)", repo.linhas.getValue(2).U_Resumo)

        s.removerPerfil(admin, "conferente", 2, null)
        assertFalse(s.atual().documento.perfis.containsKey("conferente"))
        assertEquals("conferente(removido -1)", repo.linhas.getValue(3).U_Resumo)
    }

    @Test
    fun `perfis usados pelo sistema nao podem ser excluidos e perfil inexistente da nao encontrado`() {
        val s = semeado()
        assertThrows(RegrasValidacaoException::class.java) { s.removerPerfil(admin, "cobranca", 1, null) }
        assertThrows(RegrasValidacaoException::class.java) { s.removerPerfil(admin, "admin", 1, null) }
        assertThrows(RegrasNaoEncontradasException::class.java) { s.removerPerfil(admin, "nao_existe", 1, null) }
        assertEquals(1, repo.linhas.size)
    }

    // ------------------------------------------------------------------ o caso do print

    @Test
    fun `simular reproduz o 403 do arquivo antigo e mostra a regra depois da correcao`() {
        val s = semeado()
        val perfis = listOf("cobranca", "vendedor_admin", "pix")

        val antes = s.simular(perfis, "get", "/sales-person/137", null)
        assertFalse(antes.autorizado)
        assertNull(antes.regra)

        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/sales-person/*", "get"), 1, null)

        val depois = s.simular(perfis, "get", "/sales-person/137", null)
        assertTrue(depois.autorizado)
        assertEquals("cobranca", depois.regra!!.perfil)
        assertEquals("/sales-person/*", depois.regra!!.url)
    }

    @Test
    fun `simular aceita a url colada do console e nao libera outros metodos nem caminhos`() {
        val s = semeado()
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/sales-person/*", "get"), 1, null)

        val colada = s.simular(listOf("cobranca"), "GET", "https://cadastro-back.sustennutri.com.br/sales-person/137?page=1#topo", null)
        assertTrue(colada.autorizado)
        assertEquals("/sales-person/137", colada.caminho)

        assertFalse(s.simular(listOf("cobranca"), "post", "/sales-person/137", null).autorizado)
        assertFalse(s.simular(listOf("cobranca"), "get", "/sales-person/137/business-partners", null).autorizado)
    }

    @Test
    fun `simular com rascunho testa sem gravar e avisa de perfil sem regras`() {
        val s = semeado()
        val atual = s.atual().documento
        val rascunho = atual.copy(perfis = atual.perfis + ("cobranca" to atual.perfis.getValue("cobranca") + regra("/sales-person/*", "get")))

        assertTrue(s.simular(listOf("cobranca"), "get", "/sales-person/137", rascunho).autorizado)
        assertFalse(s.simular(listOf("cobranca"), "get", "/sales-person/137", null).autorizado)
        assertEquals(1, repo.linhas.size)
        assertEquals(listOf("fantasma"), s.simular(listOf("fantasma"), "get", "/x", null).perfisSemRegras)
    }

    @Test
    fun `simular com rascunho invalido devolve os erros`() {
        val s = semeado()
        assertThrows(RegrasValidacaoException::class.java) {
            s.simular(listOf("admin"), "get", "/x", s.atual().documento.copy(perfis = mapOf("admin" to listOf(regra("/a", "get")))))
        }
    }

    // ------------------------------------------------------------------ historico e restaurar

    @Test
    fun `restaurar cria uma versao nova igual a antiga e nao apaga o historico`() {
        val s = semeado()
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/a", "get"), 1, null)

        val estado = s.restaurar(admin, 1, 2, null)

        assertEquals(3, estado.versao)
        assertEquals("RESTAURACAO", repo.linhas.getValue(3).U_Origem)
        assertEquals("Restaurada a versão 1", repo.linhas.getValue(3).U_Comentario)
        assertEquals(RegrasCodec.fromJson(repo.linhas.getValue(1).U_Documento!!), RegrasCodec.fromJson(repo.linhas.getValue(3).U_Documento!!))
        assertEquals(3, s.versoes().size)
        assertEquals(listOf(3, 2, 1), s.versoes().map { it.versao })
    }

    @Test
    fun `restaurar a versao que ja esta valendo nao muda nada`() {
        val s = semeado()
        assertThrows(RegrasValidacaoException::class.java) { s.restaurar(admin, 1, 1, null) }
        assertThrows(RegrasNaoEncontradasException::class.java) { s.restaurar(admin, 9, 1, null) }
    }

    @Test
    fun `a versao vem com o diff contra a anterior`() {
        val s = semeado()
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/sales-person/*", "get"), 1, null)

        assertTrue(s.versao(1).diff.all { it.tipo == "NOVO" })
        val diff2 = s.versao(2).diff.single()
        assertEquals("cobranca", diff2.perfil)
        assertEquals(listOf("/sales-person/*"), diff2.adicionadas.map { it.url })
        assertThrows(RegrasNaoEncontradasException::class.java) { s.versao(7) }
    }

    // ------------------------------------------------------------------ importar / exportar

    private val yamlNovo = """
        roles:
          cobranca:
            - url: "/sales-person/*"
              actions: ["get"]
          novo_perfil:
            - url: "/branch"
              actions: ["get"]
    """.trimIndent()

    @Test
    fun `importar simulando devolve a previa e nao grava`() {
        val s = semeado()
        val previa = s.importar(admin, yamlNovo, "mesclar", true, null, null)

        assertEquals(1, previa.versaoAtual)
        assertEquals(setOf("cobranca", "novo_perfil"), previa.diff.map { it.perfil }.toSet())
        assertTrue(previa.erros.isEmpty())
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `importar mesclando grava sem tirar o que so existe no arquivo atual`() {
        val s = semeado()
        s.importar(admin, yamlNovo, "mesclar", false, 1, "traz o do repo")

        val v2 = repo.linhas.getValue(2)
        assertEquals("IMPORTACAO", v2.U_Origem)
        assertEquals("traz o do repo", v2.U_Comentario)
        val doc = s.atual().documento
        assertTrue(doc.perfis.containsKey("novo_perfil"))
        assertTrue(doc.perfis.getValue("vendedor_admin").any { it.url == "/pedido-venda/**" })
        assertTrue(doc.perfis.getValue("cobranca").any { it.url == "/sales-person/*" })
    }

    @Test
    fun `importar substituindo sem o admin e recusado na previa e na gravacao`() {
        val s = semeado()
        val previa = s.importar(admin, yamlNovo, "substituir", true, null, null)
        assertTrue(previa.erros.any { it.contains("admin") })

        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, yamlNovo, "substituir", false, 1, null) }
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `gravar a importacao exige a versao base atual`() {
        val s = semeado()
        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, yamlNovo, "mesclar", false, null, null) }
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/a", "get"), 1, null)
        assertThrows(RegrasConflitoException::class.java) { s.importar(admin, yamlNovo, "mesclar", false, 1, null) }
    }

    @Test
    fun `yaml invalido vira erro na previa e 400 na gravacao, modo invalido e recusado`() {
        val s = semeado()
        val previa = s.importar(admin, "roles: [", "mesclar", true, null, null)
        assertTrue(previa.erros.single().startsWith("YAML invalido"))

        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, "roles: [", "mesclar", false, 1, null) }
        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, yamlNovo, "apagar-tudo", true, null, null) }
    }

    @Test
    fun `importar um yaml que nao muda nada nao cria versao`() {
        val s = semeado()
        val igual = s.exportar(null)
        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, igual, "substituir", false, 1, null) }
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `exportar devolve yaml no schema antigo, da versao pedida ou da atual`() {
        val s = semeado()
        val v1 = s.exportar(1)
        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/sales-person/*", "get"), 1, "nome do vendedor")

        assertEquals(v1, s.exportar(1))
        assertFalse(s.exportar(1).contains("/sales-person/*\""))
        assertTrue(s.exportar(null).contains("/sales-person/*"))
        assertFalse(s.exportar(null).contains("comentario:"))
        assertEquals(yamlSwarm, s.textoDoArquivo())
    }

    // ------------------------------------------------------------------ perfis protegidos, notas, limite

    private val yamlRepo = RegrasAcessoServiceTest::class.java.getResource("/rules.yml")!!.readText()

    @Test
    fun `importar substituindo nao remove perfil usado pelo sistema`() {
        val s = semeado()
        val soAdminECobranca = "roles:\n  admin:\n    - url: \"/**\"\n      actions: [\"*\"]\n  cobranca:\n    - url: \"/cobranca/**\"\n      actions: [\"get\"]\n"

        val previa = s.importar(admin, soAdminECobranca, "substituir", true, null, null)
        assertTrue(previa.erros.any { it.contains("pix") && it.contains("não podem sair") })

        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, soAdminECobranca, "substituir", false, 1, null) }
        assertEquals(1, repo.linhas.size)
    }

    @Test
    fun `restaurar uma versao antiga nao remove perfil que entrou depois`() {
        val s = semeado()
        // o arquivo do swarm nao tem liberacao_trava; o do repo tem
        s.importar(admin, yamlRepo, "mesclar", false, 1, null)
        assertTrue(s.atual().documento.perfis.containsKey("liberacao_trava"))

        val erro = assertThrows(RegrasValidacaoException::class.java) { s.restaurar(admin, 1, 2, null) }
        assertTrue(erro.erros.single().contains("liberacao_trava"))
        assertEquals(2, repo.linhas.size)
    }

    @Test
    fun `mudar so a nota de uma regra e uma edicao gravavel`() {
        val s = semeado()
        val comNota = regrasDe(s, "cobranca").map { it.copy(comentario = "só para a cobrança") }

        val estado = s.salvarPerfil(admin, "cobranca", comNota, 1, "documenta")

        assertEquals(2, estado.versao)
        assertEquals("sem mudança de acesso (só notas ou ordem)", repo.linhas.getValue(2).U_Resumo)
        assertEquals("só para a cobrança", regrasDe(s, "cobranca").first().comentario)
        // repetir a mesma edicao ai sim nao muda nada
        assertThrows(RegrasValidacaoException::class.java) { s.salvarPerfil(admin, "cobranca", comNota, 2, null) }
    }

    @Test
    fun `importar um yaml que so acrescenta nota tem mudanca mesmo sem diff de acesso`() {
        val s = semeado()
        val soNota = "roles:\n  cobranca:\n    # só uma nota\n    - url: \"/cobranca/**\"\n      actions: [\"get\", \"post\"]\n"

        val previa = s.importar(admin, soNota, "mesclar", true, null, null)
        assertTrue(previa.diff.isEmpty())
        assertTrue(previa.temMudanca)

        s.importar(admin, soNota, "mesclar", false, 1, null)
        assertEquals("só uma nota", regrasDe(s, "cobranca").first { it.url == "/cobranca/**" }.comentario)
    }

    @Test
    fun `no limite de versoes do Code a gravacao e recusada em vez de quebrar a ordenacao`() {
        repo.inserir(999_999, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("admin" to listOf(RegraDoc("/**", listOf("*")))))))
        val s = servico()

        val erro = assertThrows(RegrasValidacaoException::class.java) {
            s.salvarPerfil(admin, "novo", listOf(regra("/a", "get")), 999_999, null)
        }
        assertTrue(erro.erros.single().contains("limite"))
        assertEquals(1, repo.linhas.size)
    }

    // ------------------------------------------------------------------ cache e fonte

    @Test
    fun `com a fonte sap a gravacao vale na hora nesta instancia e a tela informa`() {
        val cache = RegrasAcessoCache(repo, 0)
        servico().semearSeVazio()
        cache.carregarNoBoot()
        val s = servico(cache = cache)

        assertEquals("sap", s.atual().fonteAtiva)
        assertEquals(1, s.atual().versaoEmVigor)
        assertTrue(SapRuleService(cache).get("cobranca").none { it.url == "/sales-person/*" })

        s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/sales-person/*", "get"), 1, null)

        assertEquals(2, s.atual().versaoEmVigor)
        assertTrue(SapRuleService(cache).get("cobranca").any { it.url == "/sales-person/*" })
    }

    @Test
    fun `com a fonte arquivo a tela informa que o cadastro ainda nao vale`() {
        val s = semeado()
        val estado = s.atual()
        assertEquals("arquivo", estado.fonteAtiva)
        assertNull(estado.versaoEmVigor)
        assertTrue(estado.perfisProtegidos.containsAll(listOf("admin", "cobranca", "pix")))
        assertEquals("memoria:rules.yml", estado.arquivoLocal)
    }

    // ------------------------------------------------------------------ cobertura

    @Test
    fun `cobertura mostra quais endpoints reais uma regra alcanca`() {
        val s = semeado()
        val endpoints = listOf(
            br.andrew.sap.services.security.acesso.EndpointConhecido("/sales-person/*", "get"),
            br.andrew.sap.services.security.acesso.EndpointConhecido("/sales-person/*/business-partners", "get"),
            br.andrew.sap.services.security.acesso.EndpointConhecido("/branch", "get"),
        )
        assertEquals(listOf("/sales-person/*"), s.cobertura("/sales-person/{id}", endpoints).map { it.url })
        assertEquals(2, s.cobertura("/sales-person/**", endpoints).size)
        assertTrue(s.cobertura("/sales-person/{", endpoints).isEmpty())
    }
}
