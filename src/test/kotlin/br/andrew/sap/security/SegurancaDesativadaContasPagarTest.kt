package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.CorsConfig
import br.andrew.sap.infrastructure.security.SecurityWebConf
import br.andrew.sap.infrastructure.security.SegurancaDesativadaContasPagar
import br.andrew.sap.infrastructure.security.jwt.JwtSecretBean
import br.andrew.sap.infrastructure.security.keycloak.KeycloakJwtService
import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import br.andrew.sap.infrastructure.security.keycloak.KeycloakUserMapper
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.OneTimePasswordService
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.UserPasswordService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.SystemEnvironmentPropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockServletContext
import org.springframework.security.core.Authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.support.GenericWebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc

/**
 * Chave `sap-rovema.seguranca.desativada` / `SAP_ROVEMA_SECURITY_DISABLED` (teste local/homolog): cadeia de
 * seguranca real (SecurityWebConf com rules.yml), so o POST da consulta de contas a pagar e liberado.
 */
@ExtendWith(OutputCaptureExtension::class)
class SegurancaDesativadaContasPagarTest {
    private val abertos = mutableListOf<GenericWebApplicationContext>()

    @AfterEach
    fun fechar() = abertos.forEach { it.close() }

    /**
     * [ambiente] simula variaveis de ambiente reais (SystemEnvironmentPropertySource, com o relaxed
     * binding do Spring: `SEGURANCA_DESATIVADA` responde por `seguranca.desativada`).
     */
    private fun contexto(
        propriedades: Map<String, Any>,
        vararg perfis: String,
        ambiente: Map<String, Any> = emptyMap(),
        perfisPadrao: List<String> = emptyList(),
    ): GenericWebApplicationContext {
        val keycloak = KeycloakProperties()
        val context = GenericWebApplicationContext().apply {
            servletContext = MockServletContext()
            environment.propertySources.addFirst(SystemEnvironmentPropertySource("ambiente-teste", ambiente))
            environment.propertySources.addFirst(MapPropertySource("teste", propriedades))
            environment.setActiveProfiles(*perfis)
            if (perfisPadrao.isNotEmpty()) environment.setDefaultProfiles(*perfisPadrao.toTypedArray())
            beanFactory.registerSingleton("jwtSecretBean", JwtSecretBean("chave-hmac-somente-de-teste-".repeat(4)))
            beanFactory.registerSingleton("ruleService", RegrasArquivoService(DefaultResourceLoader()))
            beanFactory.registerSingleton("userPasswordService", mock(UserPasswordService::class.java))
            beanFactory.registerSingleton("oneTimePasswordService", mock(OneTimePasswordService::class.java))
            beanFactory.registerSingleton("keycloakProperties", keycloak)
            beanFactory.registerSingleton("keycloakJwtService", KeycloakJwtService(keycloak))
            beanFactory.registerSingleton("keycloakUserMapper", mock(KeycloakUserMapper::class.java))
            beanFactory.registerSingleton("corsConfig", CorsConfig(listOf("http://localhost:4200")))
            AnnotatedBeanDefinitionReader(this).register(SegurancaDesativadaContasPagar::class.java,
                SecurityWebConf::class.java, MvcConfig::class.java, Rotas::class.java)
        }
        abertos += context
        context.refresh()
        return context
    }

    private fun mvc(propriedades: Map<String, Any>, vararg perfis: String, ambiente: Map<String, Any> = emptyMap()): MockMvc =
        mvc(contexto(propriedades, *perfis, ambiente = ambiente))

    private fun mvc(context: GenericWebApplicationContext): MockMvc {
        return MockMvcBuilders.webAppContextSetup(context)
            .addFilters<DefaultMockMvcBuilder>(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter::class.java))
            .build()
    }

    /** Barrado pela seguranca (401 ou 403, conforme o entry point), nunca executado. */
    private val barrado = ResultMatcher { r ->
        assertTrue(r.response.status in setOf(401, 403), "esperado 401/403, veio ${r.response.status}")
    }

    private val consulta = SegurancaDesativadaContasPagar.CAMINHO

    @Test
    fun `desligada por padrao a consulta continua exigindo autenticacao`() {
        mvc(emptyMap()).perform(post(consulta)).andExpect(barrado)
    }

    @Test
    fun `explicitamente false prevalece sobre a variavel`() {
        mvc(mapOf("sap-rovema.seguranca.desativada" to "false", "SAP_ROVEMA_SECURITY_DISABLED" to "true"))
            .perform(post(consulta)).andExpect(barrado)
    }

    @Test
    fun `ligada libera so o POST da consulta com usuario sintetico da integracao`(output: CapturedOutput) {
        val mvc = mvc(mapOf("SAP_ROVEMA_SECURITY_DISABLED" to "true"))
        mvc.perform(post(consulta).contentType(MediaType.APPLICATION_JSON).content("""{"cnpjFornecedor":"dado-nao-logado"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario").value(SegurancaDesativadaContasPagar.PRINCIPAL))
            .andExpect(jsonPath("$.roles[0]").value("integracao_contas_pagar"))
            .andExpect(jsonPath("$.roles.length()").value(1))
        assertTrue(output.out.contains("SEGURANCA DESATIVADA (SAP_ROVEMA_SECURITY_DISABLED)"))
        assertTrue(output.out.contains("SEGURANCA DESATIVADA: POST $consulta aceita sem autenticacao"))
        assertFalse(output.out.contains("dado-nao-logado"))
    }

    @Test
    fun `variavel de ambiente real SAP_ROVEMA_SECURITY_DISABLED liga a chave`() {
        mvc(emptyMap(), ambiente = mapOf("SAP_ROVEMA_SECURITY_DISABLED" to "true"))
            .perform(post(consulta)).andExpect(status().isOk)
            .andExpect(jsonPath("$.usuario").value(SegurancaDesativadaContasPagar.PRINCIPAL))
    }

    @Test
    fun `SEGURANCA_DESATIVADA generico nao liga a chave`() {
        val context = contexto(emptyMap(), ambiente = mapOf("SEGURANCA_DESATIVADA" to "true"))
        // O relaxed binding esta ativo (o nome antigo seria resolvido), mas a chave continua desligada.
        assertEquals("true", context.environment.getProperty("seguranca.desativada"))
        assertFalse(context.getBean(SegurancaDesativadaContasPagar::class.java).ativa)
        mvc(context).perform(post(consulta)).andExpect(barrado)
    }

    @Test
    fun `propriedade generica seguranca_desativada nao liga a chave`() {
        mvc(mapOf("seguranca.desativada" to "true")).perform(post(consulta)).andExpect(barrado)
    }

    @Test
    fun `ligada nao libera outro metodo outra rota nem caminho parecido`() {
        val mvc = mvc(mapOf("sap-rovema.seguranca.desativada" to "true"))
        mvc.perform(get(consulta)).andExpect(barrado)
        mvc.perform(post("/journal/save")).andExpect(barrado)
        mvc.perform(post("$consulta-x")).andExpect(barrado)
    }

    @Test
    fun `ligada com Authorization enviado nao usa o usuario sintetico`() {
        mvc(mapOf("sap-rovema.seguranca.desativada" to "true"))
            .perform(post(consulta).header(HttpHeaders.AUTHORIZATION, "Bearer nao-e-jwt")).andExpect(barrado)
    }

    @Test
    fun `ligada com profile de producao a aplicacao nao sobe`() {
        listOf("prd", "prod", "production", "PRD").forEach { perfil ->
            val erro = assertThrows<Exception> { contexto(mapOf("SAP_ROVEMA_SECURITY_DISABLED" to "true"), "hmg", perfil) }
            val causas = generateSequence<Throwable>(erro) { it.cause }.mapNotNull { it.message }.joinToString(" | ")
            assertTrue(causas.contains("nao e permitido com o profile [$perfil]"), causas)
        }
    }

    @Test
    fun `ligada com profile padrao de producao a aplicacao nao sobe`() {
        // environment.setDefaultProfiles e SPRING_PROFILES_DEFAULT (spring.profiles.default)
        val viaApi = assertThrows<Exception> {
            contexto(mapOf("SAP_ROVEMA_SECURITY_DISABLED" to "true"), perfisPadrao = listOf("prd"))
        }
        val viaVariavel = assertThrows<Exception> {
            contexto(emptyMap(), ambiente = mapOf("SAP_ROVEMA_SECURITY_DISABLED" to "true", "SPRING_PROFILES_DEFAULT" to "prd"))
        }
        listOf(viaApi, viaVariavel).forEach { erro ->
            val causas = generateSequence<Throwable>(erro) { it.cause }.mapNotNull { it.message }.joinToString(" | ")
            assertTrue(causas.contains("nao e permitido com o profile [prd]"), causas)
        }
    }

    @Test
    fun `desligada com profile de producao sobe normalmente`() {
        mvc(emptyMap(), "prd").perform(post(consulta)).andExpect(barrado)
    }

    @Configuration
    @EnableWebMvc
    class MvcConfig

    @RestController
    class Rotas {
        private fun quem(auth: Authentication) = mapOf("usuario" to auth.name, "roles" to (auth as User).roles)

        @PostMapping(SegurancaDesativadaContasPagar.CAMINHO)
        fun consulta(auth: Authentication) = quem(auth)

        @GetMapping(SegurancaDesativadaContasPagar.CAMINHO)
        fun consultaGet(auth: Authentication) = quem(auth)

        @PostMapping("/journal/save", "${SegurancaDesativadaContasPagar.CAMINHO}-x")
        fun outra(auth: Authentication) = quem(auth)
    }
}
