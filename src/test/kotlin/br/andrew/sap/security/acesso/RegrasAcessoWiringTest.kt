package br.andrew.sap.security.acesso

import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import br.andrew.sap.services.security.acesso.RegrasAcessoFonteConfiguration
import br.andrew.sap.services.security.acesso.RegrasAcessoRepositorio
import br.andrew.sap.services.security.acesso.RegrasAcessoService
import br.andrew.sap.services.security.acesso.RegrasCodec
import br.andrew.sap.services.security.acesso.RegrasDocumento
import br.andrew.sap.services.security.acesso.SapRuleService
import br.andrew.sap.services.security.interfaces.RuleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.util.function.Supplier

/**
 * A fiacao do Spring, que os testes unitarios nao veem: qual RuleService o filtro recebe, quando o
 * cache existe e o que acontece com configuracao incoerente. O erro que isto evita e o pior possivel -
 * a tela dizer "sap" enquanto o filtro segue aplicando o arquivo (ou o contrario).
 *
 * O AcessoRegrasSeeder e o AcessoRegrasConfiguration nao entram aqui: falam com o SAP de verdade e
 * ja tem a mesma guarda de profile das outras UDOs.
 */
class RegrasAcessoWiringTest {

    private val repo = RepositorioEmMemoria().also {
        it.inserir(1, RegrasCodec.toJson(RegrasDocumento(perfis = mapOf("admin" to listOf(RegraDoc("/**", listOf("*")))))))
    }

    private fun runner() = ApplicationContextRunner()
        .withUserConfiguration(RegrasArquivoService::class.java, RegrasAcessoFonteConfiguration::class.java)
        .withBean(RegrasAcessoRepositorio::class.java, Supplier { repo })

    @Test
    fun `por padrao so existe o arquivo e nao ha cache`() {
        runner().run { ctx ->
            assertNotNull(ctx.getBean(RuleService::class.java))
            assertInstanceOf(RegrasArquivoService::class.java, ctx.getBean(RuleService::class.java))
            assertTrue(ctx.getBeansOfType(RegrasAcessoCache::class.java).isEmpty())
        }
    }

    @Test
    fun `fonte arquivo explicita continua sendo o arquivo`() {
        runner().withPropertyValues("acesso.regras.fonte=arquivo").run { ctx ->
            assertInstanceOf(RegrasArquivoService::class.java, ctx.getBean(RuleService::class.java))
            assertTrue(ctx.getBeansOfType(RegrasAcessoCache::class.java).isEmpty())
        }
    }

    @Test
    fun `fonte sap troca o RuleService que o filtro recebe, mesmo com o arquivo tambem registrado`() {
        runner().withPropertyValues("acesso.regras.fonte=sap").run { ctx ->
            assertEquals(2, ctx.getBeansOfType(RuleService::class.java).size)
            assertInstanceOf(SapRuleService::class.java, ctx.getBean(RuleService::class.java))
            assertEquals(1, ctx.getBean(RegrasAcessoCache::class.java).atual().versao)
            assertEquals(listOf("/**"), ctx.getBean(RuleService::class.java).get("admin").map { it.url })
        }
    }

    @Test
    fun `fonte sap sem repositorio falha o boot dizendo o que ligar`() {
        ApplicationContextRunner()
            .withUserConfiguration(RegrasArquivoService::class.java, RegrasAcessoFonteConfiguration::class.java)
            .withPropertyValues("acesso.regras.fonte=sap")
            .run { ctx ->
                assertNotNull(ctx.startupFailure)
                assertTrue(generateSequence(ctx.startupFailure) { it.cause }.any { it.message?.contains("fields.acesso=true") == true })
            }
    }

    @Test
    fun `fonte sap com o SAP sem nenhuma versao falha o boot`() {
        repo.linhas.clear()
        runner().withPropertyValues("acesso.regras.fonte=sap").run { ctx ->
            assertNotNull(ctx.startupFailure)
        }
    }

    @Test
    fun `o servico de gestao existe so com fields acesso e informa a fonte que o filtro usa`() {
        runner().run { ctx -> assertTrue(ctx.getBeansOfType(RegrasAcessoService::class.java).isEmpty()) }

        runner()
            .withUserConfiguration(RegrasAcessoService::class.java)
            .withPropertyValues("fields.acesso=true")
            .run { ctx ->
                val service = ctx.getBean(RegrasAcessoService::class.java)
                assertEquals("arquivo", service.atual().fonteAtiva)
                assertFalse(ctx.getBeansOfType(RegrasAcessoCache::class.java).isNotEmpty())
            }

        runner()
            .withUserConfiguration(RegrasAcessoService::class.java)
            .withPropertyValues("fields.acesso=true", "acesso.regras.fonte=sap")
            .run { ctx ->
                val estado = ctx.getBean(RegrasAcessoService::class.java).atual()
                assertEquals("sap", estado.fonteAtiva)
                assertEquals(1, estado.versaoEmVigor)
            }
    }
}
