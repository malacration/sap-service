package br.andrew.sap.infrastructure.security.keycloak

import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.cadastro.EmployeesInfoService
import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Perfil integracao_contas_pagar so vale no token da service account do client permitido
 * (keycloak.integracao-contas-pagar.client-ids); os demais perfis nao mudam.
 */
class KeycloakUserMapperIntegracaoTest {

    private val rules = object : RuleService {
        override fun get(role: String) =
            if (role == "producao" || role == "integracao_contas_pagar") listOf(Rule("/**", "*")) else listOf()
    }
    private val properties = KeycloakProperties().apply { clientId = "sapvema" }
    private val employees = mock<EmployeesInfoService> { on { getSalesPersonCodeByEmployeeId(any()) } doReturn null }
    private val places = mock<BussinessPlaceService> { on { getFilialByEmployee(any()) } doReturn listOf() }
    private val mapper = KeycloakUserMapper(properties, employees, rules, places)

    private fun claims(extra: Map<String, Any>, vararg roles: String, realm: List<String> = listOf()) = Jwts.claims()
        .subject("u1")
        .add("sap_code", "606")
        .add("resource_access", mapOf("sapvema" to mapOf("roles" to roles.toList())))
        .add("realm_access", mapOf("roles" to realm))
        .add(extra)
        .build()

    private fun sa(client: String) = mapOf("azp" to client, "preferred_username" to "service-account-$client", "client_id" to client)

    @Test
    fun `service account do client permitido recebe o perfil de integracao`() {
        assertEquals(listOf("integracao_contas_pagar"),
            mapper.toUser(claims(sa("nota-agil-integracao"), "integracao_contas_pagar")).roles)
        assertEquals(listOf("integracao_contas_pagar"),
            mapper.toUser(claims(sa("nota-agil-integracao"), realm = listOf("integracao_contas_pagar"))).roles)
        // Keycloak antigo (clientId) e username sem diferenciar maiusculas.
        assertEquals(listOf("integracao_contas_pagar"), mapper.toUser(claims(mapOf("azp" to "nota-agil-integracao",
            "preferred_username" to "Service-Account-Nota-Agil-Integracao", "clientId" to "nota-agil-integracao"),
            "integracao_contas_pagar")).roles)
    }

    @Test
    fun `token de outro client com a role nao recebe o perfil`() {
        assertThrows<KeycloakUserProvisioningException> {
            mapper.toUser(claims(sa("outro-client"), "integracao_contas_pagar"))
        }
        // azp forjado sem ser a service account dele.
        assertThrows<KeycloakUserProvisioningException> {
            mapper.toUser(claims(mapOf("azp" to "nota-agil-integracao", "preferred_username" to "service-account-outro-client"),
                "integracao_contas_pagar"))
        }
        assertThrows<KeycloakUserProvisioningException> {
            mapper.toUser(claims(sa("nota-agil-integracao") + ("client_id" to "outro-client"), "integracao_contas_pagar"))
        }
    }

    @Test
    fun `usuario comum com a role perde so o perfil de integracao`() {
        // Pelo front ou logado pelo proprio client permitido: nao e service account.
        for (azp in listOf("sapvema", "nota-agil-integracao")) {
            val usuario = mapOf("azp" to azp, "preferred_username" to "fulano")
            assertThrows<KeycloakUserProvisioningException> {
                mapper.toUser(claims(usuario, "integracao_contas_pagar", realm = listOf("integracao_contas_pagar")))
            }
            assertEquals(listOf("producao"), mapper.toUser(claims(usuario, "producao", "integracao_contas_pagar")).roles)
        }
    }

    @Test
    fun `demais perfis nao dependem de service account`() {
        assertEquals(listOf("producao"), mapper.toUser(claims(mapOf("azp" to "sapvema"), "producao")).roles)
    }

    @Test
    fun `role protegida e o perfil do rules yml`() {
        val rulesYml = javaClass.classLoader.getResource("rules.yml")!!.readText()
        assertEquals(true, rulesYml.lines().any { it == "  ${KeycloakUserMapper.ROLE_INTEGRACAO_CONTAS_PAGAR}:" })
    }
}
