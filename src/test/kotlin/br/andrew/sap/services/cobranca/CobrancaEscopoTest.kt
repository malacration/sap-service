package br.andrew.sap.services.cobranca

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CobrancaEscopoTest {

    private fun usuario(id: String, roles: List<String>) = User(
        id, "Fulano", UserOriginEnum.EmployeesInfo, "fulano",
        bussinesPlace = listOf(), roles = roles
    )

    @Test
    fun `admin ve o vendedor solicitado, sem restricao`() {
        val admin = usuario("1", listOf("admin"))
        assertEquals(42, CobrancaEscopo.vendedorEfetivo(admin, 42))
        assertEquals(null, CobrancaEscopo.vendedorEfetivo(admin, null))
    }

    @Test
    fun `vendedor_admin ve o vendedor solicitado, sem restricao`() {
        val vendedorAdmin = usuario("2", listOf("vendedor_admin"))
        assertEquals(42, CobrancaEscopo.vendedorEfetivo(vendedorAdmin, 42))
        assertEquals(null, CobrancaEscopo.vendedorEfetivo(vendedorAdmin, null))
    }

    @Test
    fun `cobranca sem SalesPersonCode vinculado ve o vendedor solicitado, nao e restringido pelo EmployeeID`() {
        val cobranca = usuario("999", listOf("cobranca"))
        assertEquals(42, CobrancaEscopo.vendedorEfetivo(cobranca, 42))
        assertEquals(null, CobrancaEscopo.vendedorEfetivo(cobranca, null))
    }

    @Test
    fun `vendedor comum e restringido ao proprio id, mesmo pedindo outro vendedor`() {
        val vendedor = usuario("60", listOf("vendedor"))
        assertEquals(60, CobrancaEscopo.vendedorEfetivo(vendedor, 999))
        assertEquals(60, CobrancaEscopo.vendedorEfetivo(vendedor, null))
    }
}
