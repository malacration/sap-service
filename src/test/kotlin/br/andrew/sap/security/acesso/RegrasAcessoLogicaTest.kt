package br.andrew.sap.security.acesso

import br.andrew.sap.infrastructure.security.RulePathMatcher
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasCodec
import br.andrew.sap.services.security.acesso.RegrasDiff
import br.andrew.sap.services.security.acesso.RegrasDocumento
import br.andrew.sap.services.security.acesso.RegrasValidador
import br.andrew.sap.services.security.acesso.RegrasYaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.core.io.DefaultResourceLoader
import java.io.File

/**
 * Logica pura da gestao de regras: YAML no schema antigo, validador, diff e mescla. As duas fixtures
 * sao o rules.yml do repo e o do swarm como estava ANTES do ajuste de sales-person por id (o que
 * producao realmente rodava quando o 403 apareceu).
 */
class RegrasAcessoLogicaTest {

    private val yamlRepo = lerRecurso("/rules.yml")
    private val yamlSwarm = lerRecurso("/fixtures/rules-swarm-antes-sales-person.yml")

    private fun lerRecurso(caminho: String) =
        RegrasAcessoLogicaTest::class.java.getResource(caminho)!!.readText()

    private fun doc(vararg perfis: Pair<String, List<RegraDoc>>) = RegrasDocumento(perfis = linkedMapOf(*perfis))

    private fun regra(url: String, vararg acoes: String, comentario: String? = null) =
        RegraDoc(url, acoes.toList(), comentario)

    private val adminOk = "admin" to listOf(regra("/**", "*"))

    // ------------------------------------------------------------------ arquivos reais

    @Test
    fun `o validador aceita o arquivo do repo e o do swarm`() {
        listOf(yamlRepo, yamlSwarm).forEach { yaml ->
            val resultado = RegrasValidador.validar(RegrasYaml.importar(yaml))
            assertTrue(resultado.ok, "erros: ${resultado.erros}")
        }
    }

    @Test
    fun `avisa dos perfis do sistema sem regras no arquivo do swarm`() {
        val avisos = RegrasValidador.validar(RegrasYaml.importar(yamlSwarm)).avisos
        assertTrue(avisos.any { it.contains("'liberacao_trava'") })
        assertTrue(avisos.any { it.contains("'caixa'") })
    }

    @Test
    fun `exportar e importar de volta da o mesmo conteudo`(@TempDir pasta: File) {
        listOf(yamlRepo, yamlSwarm).forEach { yaml ->
            val original = arquivoService(pasta, yaml)
            val exportado = RegrasYaml.exportar(RegrasYaml.importar(yaml))
            val relido = arquivoService(pasta, exportado)

            assertEquals(original.todas(), relido.todas())
            assertEquals(original.todas().keys.toList(), relido.todas().keys.toList())
        }
    }

    @Test
    fun `o yaml exportado continua lido pelo mapper estrito do arquivo`(@TempDir pasta: File) {
        // Uma chave a mais (comentario:) derrubaria a v6; as notas saem como linhas com #.
        val doc = doc(adminOk, "cobranca" to listOf(regra("/sales-person/*", "get", comentario = "nome do vendedor")))
        val yaml = RegrasYaml.exportar(doc)

        assertFalse(yaml.contains("comentario:"))
        assertTrue(yaml.contains("# nome do vendedor"))
        assertEquals(doc.paraRegras(), arquivoService(pasta, yaml).todas())
    }

    @Test
    fun `comentarios acima da regra viram nota e voltam na exportacao`() {
        val doc = RegrasYaml.importar(yamlRepo)
        val nota = doc.perfis.getValue("vendedor").first { it.url == "/contrato-venda-futura/*/localidade" }.comentario

        assertNotNull(nota)
        assertTrue(nota!!.contains("pre-requisito da troca"))
        assertTrue(RegrasYaml.exportar(doc).contains("# pre-requisito da troca"))
    }

    @Test
    fun `yaml sem a chave roles ou malformado vira erro legivel`() {
        assertThrows(IllegalArgumentException::class.java) { RegrasYaml.importar("perfis: []") }
        assertThrows(IllegalArgumentException::class.java) { RegrasYaml.importar("roles: [") }
    }

    @Test
    fun `json do documento ignora campos que uma versao futura acrescentar`() {
        val json = """{"schema":2,"campoNovo":true,"perfis":{"admin":[{"url":"/**","actions":["*"],"extra":1}]}}"""
        val lido = RegrasCodec.fromJson(json)
        assertEquals(2, lido.schema)
        assertEquals(doc(adminOk).perfis, lido.perfis)
    }

    // ------------------------------------------------------------------ o caso real do 403

    @Test
    fun `no arquivo antigo do swarm o vendedor_admin tomava 403 em sales-person por id`() {
        val matcher = RulePathMatcher()
        val regras = RegrasYaml.importar(yamlSwarm).paraRegras()
        val doUsuario = listOf("cobranca", "vendedor_admin", "pix").flatMap { regras.getValue(it) }

        assertNull(matcher.autoriza(doUsuario, "get", "/sales-person/137"))
    }

    @Test
    fun `adicionar a regra em cobranca libera o caso`() {
        val matcher = RulePathMatcher()
        val base = RegrasYaml.importar(yamlSwarm)
        val depois = base.copy(perfis = base.perfis + ("cobranca" to base.perfis.getValue("cobranca") +
            regra("/sales-person/*", "get")))
        val regras = depois.paraRegras()
        val doUsuario = listOf("cobranca", "vendedor_admin", "pix").flatMap { regras.getValue(it) }

        assertEquals("/sales-person/*", matcher.autoriza(doUsuario, "get", "/sales-person/137")?.url)
        assertNull(matcher.autoriza(doUsuario, "post", "/sales-person/137"))
    }

    // ------------------------------------------------------------------ validador

    private fun erros(vararg perfis: Pair<String, List<RegraDoc>>) =
        RegrasValidador.validar(doc(adminOk, *perfis)).erros

    @Test
    fun `nome de perfil valido e nomes reservados do keycloak`() {
        assertTrue(erros("novo_perfil-2" to listOf(regra("/a", "get"))).isEmpty())
        listOf("Maiusculo", "com espaco", "_comeca", "filial-3-matriz", "default-roles-rovema", "offline_access", "uma_authorization")
            .forEach { assertTrue(erros(it to listOf(regra("/a", "get"))).isNotEmpty(), it) }
    }

    @Test
    fun `chaves viram asterisco com aviso e continuam validas`() {
        val r = RegrasValidador.validar(doc(adminOk, "p" to listOf(regra("/business-partners/{id}", "get"))))
        assertTrue(r.ok)
        assertEquals("/business-partners/*", r.documento.perfis.getValue("p").single().url)
        assertTrue(r.avisos.any { it.contains("{id}") })
    }

    @Test
    fun `urls e acoes invalidas sao erro`() {
        assertTrue(erros("p" to listOf(regra("sem-barra", "get"))).isNotEmpty())
        assertTrue(erros("p" to listOf(regra("/tem espaco", "get"))).isNotEmpty())
        assertTrue(erros("p" to listOf(regra("/a?x=1", "get"))).isNotEmpty())
        assertTrue(erros("p" to listOf(regra("/a", "trace"))).isNotEmpty())
        assertTrue(erros("p" to listOf(RegraDoc("/a", emptyList()))).isNotEmpty())
    }

    @Test
    fun `curingas demais ou repetidos sao recusados em vez de normalizados`() {
        listOf(
            "/a/***",                           // viraria /a/** e passaria a liberar /ab
            "/a/*****/b",
            "/*a*a*a*b",                        // 3 curingas colados no mesmo trecho
            "/**/**/**/x",                      // 3 duplos: custo cubico no caminho
            "/a/{x}{y}",                        // viraria /a/** e passaria a liberar /ab
            "/a/{x}*",
            "/**/a/**/b/**/c/**/d",             // 4 duplos
            "/*/*/*/*/*/*/*/*/*/x",             // 9 curingas no total
            "/" + "a".repeat(201),              // longo demais
        ).forEach { assertTrue(erros("p" to listOf(regra(it, "get"))).isNotEmpty(), it) }
        assertTrue(erros("p" to listOf(regra("/**/a/**", "get"))).isEmpty())
        // variavel separada por barra de outros curingas e valida
        assertTrue(erros("p" to listOf(regra("/a/{x}/**/b", "get"))).isEmpty())
        assertTrue(erros("p" to listOf(regra("/a/{x}/{y}", "get"))).isEmpty())
    }

    @Test
    fun `os arquivos reais respeitam os limites de curingas`() {
        listOf(yamlRepo, yamlSwarm).forEach {
            assertTrue(RegrasValidador.validar(RegrasYaml.importar(it)).erros.isEmpty())
        }
    }

    @Test
    fun `acoes sao normalizadas e o curinga absorve as demais`() {
        val r = RegrasValidador.validar(doc(adminOk, "p" to listOf(regra("/a", "GET", " Post "), regra("/b", "get", "*"))))
        val regras = r.documento.perfis.getValue("p")
        assertEquals(listOf("get", "post"), regras[0].actions)
        assertEquals(listOf("*"), regras[1].actions)
    }

    @Test
    fun `url repetida no mesmo perfil e mesclada com aviso`() {
        val r = RegrasValidador.validar(doc(adminOk, "p" to listOf(regra("/a", "get"), regra("/a", "post"))))
        assertTrue(r.ok)
        assertEquals(listOf("get", "post"), r.documento.perfis.getValue("p").single().actions)
        assertTrue(r.avisos.any { it.contains("mesclada") })
    }

    @Test
    fun `perfil sem regras nao existe`() {
        assertTrue(erros("p" to emptyList()).any { it.contains("sem regras") })
    }

    @Test
    fun `admin precisa manter a regra curinga`() {
        assertTrue(RegrasValidador.validar(doc("admin" to listOf(regra("/a", "get")))).erros.isNotEmpty())
        assertTrue(RegrasValidador.validar(doc("admin" to listOf(regra("/**", "get")))).erros.isNotEmpty())
        assertTrue(RegrasValidador.validar(doc("outro" to listOf(regra("/a", "get")))).erros.isNotEmpty())
        assertTrue(RegrasValidador.validar(doc(adminOk)).ok)
    }

    @Test
    fun `documento acima do limite e recusado`() {
        val grande = (1..2000).map { regra("/recurso-numero-$it/detalhe/*", "get") }
        assertTrue(erros("p" to grande).any { it.contains("limite") })
    }

    // ------------------------------------------------------------------ diff e mescla

    @Test
    fun `diff separa novo, alterado e removido e ignora so comentario`() {
        val antes = doc(adminOk, "a" to listOf(regra("/x", "get")), "b" to listOf(regra("/y", "get")))
        val depois = doc(
            adminOk,
            "a" to listOf(regra("/x", "get", "post", comentario = "nota"), regra("/z", "get")),
            "c" to listOf(regra("/w", "get")),
        )
        val mudancas = RegrasDiff.comparar(antes, depois).associateBy { it.perfil }

        assertEquals(setOf("a", "b", "c"), mudancas.keys)
        assertEquals("ALTERADO", mudancas.getValue("a").tipo)
        assertEquals(listOf("/z"), mudancas.getValue("a").adicionadas.map { it.url })
        assertEquals(listOf("/x"), mudancas.getValue("a").alteradas.map { it.url })
        assertEquals("REMOVIDO", mudancas.getValue("b").tipo)
        assertEquals("NOVO", mudancas.getValue("c").tipo)
        assertTrue(RegrasDiff.comparar(depois, depois.copy(perfis = depois.perfis.mapValues { (_, rs) -> rs.map { it.copy(comentario = "outra") } })).isEmpty())
    }

    @Test
    fun `resumo e curto e cabe na coluna do sap`() {
        val antes = doc(adminOk)
        val depois = doc(adminOk, "cobranca" to listOf(regra("/a", "get")))
        assertEquals("cobranca(novo +1)", RegrasDiff.resumo(RegrasDiff.comparar(antes, depois)))
        assertEquals("sem mudancas", RegrasDiff.resumo(emptyList()))
        val muitos = (1..200).map { "perfil_$it" to listOf(regra("/a", "get")) }.toTypedArray()
        assertTrue(RegrasDiff.resumo(RegrasDiff.comparar(antes, doc(adminOk, *muitos))).length <= 254)
    }

    @Test
    fun `mesclar une por perfil e url sem remover nada`() {
        val base = doc(adminOk, "p" to listOf(regra("/a", "get"), regra("/so-em-producao", "*")))
        val extra = doc("p" to listOf(regra("/a", "post"), regra("/b", "get")), "q" to listOf(regra("/c", "get")))
        val mesclado = RegrasDiff.mesclar(base, extra)

        assertEquals(listOf("admin", "p", "q"), mesclado.perfis.keys.toList())
        val p = mesclado.perfis.getValue("p").associateBy { it.url }
        assertEquals(listOf("get", "post"), p.getValue("/a").actions)
        assertEquals(listOf("*"), p.getValue("/so-em-producao").actions)
        assertEquals(listOf("/a", "/so-em-producao", "/b"), mesclado.perfis.getValue("p").map { it.url })
    }

    @Test
    fun `mesclar o repo no swarm traz o que faltava sem tirar nada`() {
        val swarm = RegrasYaml.importar(yamlSwarm)
        val repo = RegrasYaml.importar(yamlRepo)
        val mesclado = RegrasDiff.mesclar(swarm, repo)

        assertTrue(mesclado.perfis.containsKey("liberacao_trava"))
        assertTrue(mesclado.perfis.containsKey("sysfeed"))
        // o que so o swarm tem (pedido-venda/** do vendedor_admin) continua la
        assertTrue(mesclado.perfis.getValue("vendedor_admin").any { it.url == "/pedido-venda/**" })
        assertTrue(RegrasValidador.validar(mesclado).ok)
    }

    // ------------------------------------------------------------------ apoio

    private fun arquivoService(pasta: File, conteudo: String): RegrasArquivoService {
        val arquivo = File(pasta, "rules-${System.nanoTime()}.yml").apply { writeText(conteudo) }
        return RegrasArquivoService(DefaultResourceLoader(), "file:${arquivo.absolutePath}")
    }
}
