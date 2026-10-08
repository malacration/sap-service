package br.andrew.sap.security.acesso

import br.andrew.sap.infrastructure.security.keycloak.KeycloakAdminException
import br.andrew.sap.infrastructure.security.keycloak.KeycloakRolesGateway
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
    private fun servico(
        yaml: String = yamlSwarm,
        cache: RegrasAcessoCache? = null,
        keycloak: KeycloakRolesGateway? = null,
        keycloakLigado: Boolean = false,
    ): RegrasAcessoService {
        val bf = DefaultListableBeanFactory()
        if (keycloak != null) bf.registerSingleton("keycloak", keycloak)
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
            bf.getBeanProvider(KeycloakRolesGateway::class.java),
            keycloakLigado,
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

    // ------------------------------------------------------------------ roles no Keycloak

    private class KeycloakFalso(
        val existentes: MutableSet<String> = mutableSetOf(),
        var falha: String? = null,
        val deRealm: Set<String> = emptySet(),
        /** Roda no meio de garantirRole, para o teste observar o que acontece enquanto o Keycloak esta sendo chamado. */
        var duranteChamada: (() -> Unit)? = null,
    ) : KeycloakRolesGateway {
        val chamadas = mutableListOf<String>()
        override fun garantirRole(nome: String): Boolean {
            chamadas.add(nome)
            duranteChamada?.invoke()
            falha?.let { throw KeycloakAdminException(it) }
            return existentes.add(nome)
        }
        override fun roleDeRealmExiste(nome: String): Boolean? = nome in deRealm
    }

    @Test
    fun `perfil novo cria a role no keycloak, perfil existente nao mexe la`() {
        val kc = KeycloakFalso()
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }

        val novo = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)
        assertEquals(listOf("conferente"), kc.chamadas)
        assertTrue(novo.avisos.isEmpty())
        assertTrue(novo.keycloakCriaRoles && novo.keycloakLigado)

        val edicao = s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/a", "get"), 2, null)
        assertEquals(listOf("conferente"), kc.chamadas)
        assertTrue(edicao.avisos.isEmpty())
    }

    @Test
    fun `falha no keycloak nao desfaz o perfil e vira aviso com o nome da role`() {
        val kc = KeycloakFalso(falha = "HTTP 403")
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(2, estado.versao)
        assertTrue(estado.documento.perfis.containsKey("conferente"))
        assertEquals(1, estado.avisos.size)
        assertTrue(estado.avisos.single().contains("'conferente'") && estado.avisos.single().contains("HTTP 403"))
    }

    @Test
    fun `sem a integracao mas com keycloak em uso avisa para criar a role a mao`() {
        val s = servico(keycloakLigado = true).also { it.semearSeVazio() }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(1, estado.avisos.size)
        assertTrue(estado.avisos.single().contains("Crie no Keycloak a role 'conferente'"))
        assertFalse(estado.keycloakCriaRoles)
    }

    @Test
    fun `sem keycloak em uso nao ha aviso nenhum`() {
        val s = semeado()
        assertTrue(s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null).avisos.isEmpty())
    }

    @Test
    fun `excluir perfil nunca apaga a role no keycloak, so avisa`() {
        val kc = KeycloakFalso()
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        val estado = s.removerPerfil(admin, "conferente", 2, null)

        assertEquals(listOf("conferente"), kc.chamadas)
        assertTrue(estado.avisos.single().contains("continua no Keycloak"))
    }

    @Test
    fun `role de realm com o mesmo nome avisa, mas o perfil e a role de client seguem`() {
        val kc = KeycloakFalso(deRealm = setOf("conferente"))
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(listOf("conferente"), kc.chamadas)
        assertEquals(1, estado.avisos.size)
        assertTrue(estado.avisos.single().contains("role de REALM chamada 'conferente'"))
    }

    @Test
    fun `depois da primeira falha nao insiste nos demais perfis novos`() {
        val kc = KeycloakFalso(falha = "Keycloak recusou criar a role: HTTP 403")
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        val yaml = "roles:\n  novo_a:\n    - url: \"/branch\"\n      actions: [\"get\"]\n  novo_b:\n    - url: \"/branch\"\n      actions: [\"get\"]\n  novo_c:\n    - url: \"/branch\"\n      actions: [\"get\"]\n"

        val gravada = s.importar(admin, yaml, "mesclar", false, 1, null)

        assertEquals(listOf("novo_a"), kc.chamadas)
        assertEquals(3, gravada.avisosKeycloak.size)
        assertTrue(gravada.avisosKeycloak.all { it.contains("HTTP 403") })
        assertEquals(2, s.atual().versao)
    }

    @Test
    fun `role que ja existia no keycloak avisa que quem a tem passa a receber o perfil`() {
        val kc = KeycloakFalso(existentes = mutableSetOf("conferente"))
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(1, estado.avisos.size)
        assertTrue(estado.avisos.single().contains("já existia") && estado.avisos.single().contains("'conferente'"))
    }

    @Test
    fun `se reler depois de gravar falhar, a resposta sai com o que foi gravado e o aviso do keycloak`() {
        val kc = KeycloakFalso(falha = "HTTP 403")
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        kc.duranteChamada = { repo.falhaNaLeitura = { RuntimeException("SAP caiu depois do POST") } }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(2, estado.versao)
        assertTrue(estado.documento.perfis.containsKey("conferente"))
        assertEquals(2, estado.avisos.size)
        assertTrue(estado.avisos.first().contains("HTTP 403"))
        assertTrue(estado.avisos.last().contains("Recarregue a tela"))
        assertEquals(2, repo.linhas.size)
    }

    @Test
    fun `ultima versao ilegivel - a tela abre, editar e recusado e restaurar recupera`() {
        val kc = KeycloakFalso()
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        repo.inserir(2, "isto nao e json")

        val aberta = s.atual()
        assertEquals(2, aberta.versao)
        assertTrue(aberta.documento.perfis.isEmpty())
        assertTrue(aberta.avisos.single().contains("ilegível"))

        val erro = assertThrows(RegrasValidacaoException::class.java) {
            s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 2, null)
        }
        assertTrue(erro.erros.single().contains("ilegível"))
        assertThrows(RegrasValidacaoException::class.java) { s.importar(admin, yamlRepo, "mesclar", false, 2, null) }

        val recuperada = s.restaurar(admin, 1, 2, null)

        assertEquals(3, recuperada.versao)
        assertTrue(recuperada.documento.perfis.containsKey("admin"))
        assertTrue(recuperada.avisos.isEmpty())
        assertTrue(kc.chamadas.isEmpty(), "restaurar de uma base ilegivel nao deve tentar criar todas as roles")
        assertEquals("RESTAURACAO", repo.linhas.getValue(3).U_Origem)
    }

    @Test
    fun `restaurar de uma base ilegivel nao remove perfil protegido que existia antes`() {
        val s = servico()
        // v1 sem cobranca (so admin), v2 com cobranca (o arquivo do swarm), v3 ilegivel (a ultima)
        repo.inserir(1, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("admin" to listOf(RegraDoc("/**", listOf("*")))))))
        repo.inserir(2, RegrasCodec.toJson(RegrasYaml.importar(yamlSwarm)))
        repo.inserir(3, "lixo")

        // restaurar a v1 tiraria a cobranca, que existia na ultima versao legivel (v2)
        val erro = assertThrows(RegrasValidacaoException::class.java) { s.restaurar(admin, 1, 3, null) }
        assertTrue(erro.erros.any { it.contains("cobranca") && it.contains("não podem sair") })
        assertEquals(3, repo.linhas.size)

        // restaurar a v2 (que tem tudo) recupera
        assertEquals(4, s.restaurar(admin, 2, 3, null).versao)
    }

    @Test
    fun `sem base legivel ao alcance da busca restaurar e recusado em vez de assumir vazio`() {
        val s = servico()
        repo.inserir(1, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("admin" to listOf(RegraDoc("/**", listOf("*")))))))
        repo.inserir(2, RegrasCodec.toJson(RegrasYaml.importar(yamlSwarm)))
        (3..53).forEach { repo.inserir(it, "lixo") }

        val erro = assertThrows(RegrasValidacaoException::class.java) { s.restaurar(admin, 1, 53, null) }

        assertTrue(erro.erros.single().contains("versão anterior legível"))
        assertEquals(53, repo.linhas.size)
    }

    @Test
    fun `o aviso de versao ilegivel nao se perde atras dos avisos da gravacao`() {
        val kc = KeycloakFalso(falha = "HTTP 403")
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        // outra versao, estragada, entra entre a gravacao e a releitura
        kc.duranteChamada = { repo.inserir(3, "lixo") }

        val estado = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null)

        assertEquals(2, estado.avisos.size)
        assertTrue(estado.avisos.any { it.contains("ilegível ou inválida") })
        assertTrue(estado.avisos.any { it.contains("HTTP 403") })
    }

    @Test
    fun `ultima versao bem formada mas invalida tambem e tratada como a tela de recuperacao`() {
        val s = semeado()
        repo.inserir(2, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("cobranca" to listOf(RegraDoc("/cobranca/**", listOf("get")))))))

        val aberta = s.atual()

        assertTrue(aberta.documento.perfis.isEmpty())
        assertTrue(aberta.avisos.single().contains("ilegível ou inválida"))
        assertThrows(RegrasValidacaoException::class.java) { s.salvarPerfil(admin, "x", listOf(regra("/a", "get")), 2, null) }
        assertEquals(3, s.restaurar(admin, 1, 2, null).versao)
    }

    @Test
    fun `o estado informa a versao que este backend recusou aplicar`() {
        val cache = RegrasAcessoCache(repo, 0)
        servico().semearSeVazio()
        cache.carregarNoBoot()
        val s = servico(cache = cache)
        assertNull(s.atual().versaoRejeitada)

        repo.inserir(2, "lixo")
        cache.conferir()

        assertEquals(2, s.atual().versaoRejeitada)
        assertEquals(1, s.atual().versaoEmVigor)

        // uma versao valida mais nova substitui a rejeitada
        repo.inserir(3, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("admin" to listOf(RegraDoc("/**", listOf("*")))))))
        cache.conferir()
        assertNull(s.atual().versaoRejeitada)
    }

    @Test
    fun `o simulador diz qual versao simulou e se ela ja esta valendo`() {
        val arquivoMode = semeado()
        val r1 = arquivoMode.simular(listOf("cobranca"), "get", "/cobranca/titulos", null)
        assertEquals(1, r1.versaoSimulada)
        assertEquals(false, r1.valendo)

        val cache = RegrasAcessoCache(repo, 0).also { it.carregarNoBoot() }
        val sapMode = servico(cache = cache)
        assertEquals(true, sapMode.simular(listOf("cobranca"), "get", "/cobranca/titulos", null).valendo)

        // gravada nesta instancia o cache ja avanca; uma versao mais nova so no SAP (outra instancia) ainda nao esta valendo
        repo.inserir(2, RegrasCodec.toJson(sapMode.atual().documento))
        val defasado = sapMode.simular(listOf("cobranca"), "get", "/cobranca/titulos", null)
        assertEquals(2, defasado.versaoSimulada)
        assertEquals(false, defasado.valendo)

        val rascunho = sapMode.simular(listOf("cobranca"), "get", "/cobranca/titulos", sapMode.atual().documento)
        assertNull(rascunho.versaoSimulada)
        assertNull(rascunho.valendo)
    }

    @Test
    fun `estourou o orcamento de tempo - os perfis seguintes viram aviso sem tentar`() {
        val kc = KeycloakFalso()
        kc.duranteChamada = { Thread.sleep(150) }
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio(); it.orcamentoKeycloakMs = 50 }
        val yaml = "roles:\n  novo_a:\n    - url: \"/branch\"\n      actions: [\"get\"]\n  novo_b:\n    - url: \"/branch\"\n      actions: [\"get\"]\n"

        val gravada = s.importar(admin, yaml, "mesclar", false, 1, null)

        assertEquals(listOf("novo_a"), kc.chamadas)
        assertEquals(1, gravada.avisosKeycloak.size)
        assertTrue(gravada.avisosKeycloak.single().contains("'novo_b'") && gravada.avisosKeycloak.single().contains("tempo esgotado"))
        assertEquals(2, s.atual().versao)
    }

    @Test
    fun `excecao inesperada nao mostra a mensagem original na tela`() {
        val kc = object : KeycloakRolesGateway {
            override fun garantirRole(nome: String): Boolean = throw IllegalStateException("corpo com access_token=SEGREDO")
        }
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }

        val aviso = s.salvarPerfil(admin, "conferente", listOf(regra("/branch", "get")), 1, null).avisos.single()

        assertFalse(aviso.contains("SEGREDO"))
        assertTrue(aviso.contains("veja o log do servidor"))
    }

    @Test
    fun `o keycloak e chamado com a trava das gravacoes ja solta (importacao)`() {
        val kc = KeycloakFalso()
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        var ficouPresa = false
        kc.duranteChamada = {
            // outra gravacao, em outra thread, enquanto o Keycloak "demora": se a trava ainda estivesse presa, ela nao terminaria
            val t = Thread {
                try { s.salvarPerfil(admin, "cobranca", regrasDe(s, "cobranca") + regra("/b", "get"), 2, null) } catch (_: Exception) {}
            }
            t.start()
            t.join(3000)
            if (t.isAlive) ficouPresa = true
        }
        val yaml = "roles:\n  novo_a:\n    - url: \"/branch\"\n      actions: [\"get\"]\n"

        s.importar(admin, yaml, "mesclar", false, 1, null)

        assertFalse(ficouPresa, "outra gravacao ficou presa esperando a chamada ao Keycloak")
    }

    @Test
    fun `importar e restaurar tambem criam a role dos perfis que entraram`() {
        val kc = KeycloakFalso()
        val s = servico(keycloak = kc, keycloakLigado = true).also { it.semearSeVazio() }
        val yaml = "roles:\n  novo_a:\n    - url: \"/branch\"\n      actions: [\"get\"]\n  novo_b:\n    - url: \"/branch\"\n      actions: [\"get\"]\n"

        val previa = s.importar(admin, yaml, "mesclar", true, null, null)
        assertTrue(kc.chamadas.isEmpty() && previa.avisosKeycloak.isEmpty())

        val gravada = s.importar(admin, yaml, "mesclar", false, 1, null)
        assertEquals(listOf("novo_a", "novo_b"), kc.chamadas)
        assertTrue(gravada.avisosKeycloak.isEmpty())

        s.restaurar(admin, 1, 2, null)
        assertEquals(listOf("novo_a", "novo_b"), kc.chamadas)
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
