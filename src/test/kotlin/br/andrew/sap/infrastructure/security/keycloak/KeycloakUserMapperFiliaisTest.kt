package br.andrew.sap.infrastructure.security.keycloak

import br.andrew.sap.infrastructure.configurations.CacheConfigurationProperties
import br.andrew.sap.model.sap.cadastro.Branch
import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.cadastro.EmployeesInfoService
import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.cache.annotation.Cacheable

/** Filiais do usuario SSO vem do cadastro do SAP, somadas as roles `filial-*`. */
class KeycloakUserMapperFiliaisTest {

    private val rules = object : RuleService {
        override fun get(role: String) = if (role == "producao") listOf(Rule("/**", "*")) else listOf()
    }
    private val properties = KeycloakProperties().apply { clientId = "sapvema" }

    // Mockito devolve 0 (nao null) para Int?; o servico real devolve null sem vendedor.
    private fun semVendedor() = mock<EmployeesInfoService> { on { getSalesPersonCodeByEmployeeId(any()) } doReturn null }

    private fun claims(vararg roles: String) = Jwts.claims()
        .subject("u1")
        .add("sap_code", "606")
        .add("resource_access", mapOf("sapvema" to mapOf("roles" to listOf("producao") + roles)))
        .build()

    @Test
    fun `colaborador usa a atribuicao de filial do SAP`() {
        val places = mock<BussinessPlaceService> {
            on { getFilialByEmployee(606) } doReturn listOf(Branch(2, "Matriz"), Branch(4, "AC"))
        }
        val user = KeycloakUserMapper(properties, semVendedor(), rules, places).toUser(claims())

        assertEquals(listOf(2, 4), user.bussinesPlace)
        verify(places, never()).getFilialBySalesPerson(any())
    }

    @Test
    fun `colaborador vinculado a vendedor usa a filial do vendedor e soma as roles`() {
        val employees = mock<EmployeesInfoService> { on { getSalesPersonCodeByEmployeeId(606) } doReturn 77 }
        val places = mock<BussinessPlaceService> {
            on { getFilialBySalesPerson(77) } doReturn listOf(Branch(11, "RO"))
        }
        val user = KeycloakUserMapper(properties, employees, rules, places).toUser(claims("filial-11-ro", "filial-18-rr"))

        assertEquals(listOf(11, 18), user.bussinesPlace)
    }

    @Test
    fun `falha no SAP nao derruba o login e mantem as roles`() {
        val places = mock<BussinessPlaceService> {
            on { getFilialByEmployee(any()) } doThrow RuntimeException("SAP fora")
        }
        val user = KeycloakUserMapper(properties, semVendedor(), rules, places).toUser(claims("filial-2-matriz"))

        assertEquals(listOf(2), user.bussinesPlace)
    }

    @Test
    fun `falha ao buscar o vendedor vinculado nao cai nas filiais de colaborador`() {
        val employees = mock<EmployeesInfoService> {
            on { getSalesPersonCodeByEmployeeId(any()) } doThrow RuntimeException("SAP fora")
        }
        val places = mock<BussinessPlaceService>()
        val user = KeycloakUserMapper(properties, employees, rules, places).toUser(claims("filial-2-matriz"))

        assertEquals(listOf(2), user.bussinesPlace)
        verify(places, never()).getFilialByEmployee(any())
        verify(places, never()).getFilialBySalesPerson(any())
    }

    @Test
    fun `caches das filiais estao registrados com validade curta`() {
        // Cache nao registrado no SimpleCacheManager quebra a chamada em producao.
        val expiracoes = CacheConfigurationProperties(300).getCacheExpirations()
        listOf("getFilialBySalesPerson", "getFilialByEmployee")
            .flatMap { BussinessPlaceService::class.java.getMethod(it, Int::class.javaPrimitiveType)
                .getAnnotation(Cacheable::class.java).value.toList() }
            .forEach { assertEquals(5 * 60L, expiracoes[it], it) }
    }
}
