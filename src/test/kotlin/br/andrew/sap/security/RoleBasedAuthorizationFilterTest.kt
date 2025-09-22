package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RoleBasedAuthorizationFilterTest {

    val semRole = User("windson", "windson",UserOriginEnum.EmployeesInfo,"", "","", listOf())
    val admin = User("windson", "windson",UserOriginEnum.EmployeesInfo,"", "","", listOf("admin"))
    val vendedor = User("windson", "windson",UserOriginEnum.EmployeesInfo,"", "","",listOf("vendedor"))
    val vendedor_admin = User("windson", "windson",UserOriginEnum.EmployeesInfo,"", "","",listOf("vendedor_admin"))
    val cliente = User("windson", "windson",UserOriginEnum.EmployeesInfo,"", "","", listOf("cliente"))

    val autorization = RoleBasedAuthorizationFilter(MockRuleService(),"")

    @Test
    fun semRole(){
        val urlAcessada = "/invoice"
        val resultado = autorization.isAuthorized(urlAcessada,"get",semRole)
        Assertions.assertEquals(false,resultado)
    }

    @Test
    fun adminTrue(){
        val urlAcessada = "/invoice"
        val resultado = autorization.isAuthorized(urlAcessada,"get",admin)
        Assertions.assertEquals(true,resultado)
    }

    @Test
    fun vendedorTrue(){
        val urlAcessada = "/clientes/666"
        val resultado = autorization.isAuthorized(urlAcessada,"get",vendedor)
        Assertions.assertEquals(true,resultado)
    }

    @Test
    fun vendedorFalse(){
        val urlAcessada = "/clientes/666"
        val resultado = autorization.isAuthorized(urlAcessada,"post",vendedor)
        Assertions.assertEquals(false,resultado)
    }

    @Test
    fun vendedorAdminTrue(){
        val urlAcessada = "/clientes/666"
        val resultado = autorization.isAuthorized(urlAcessada,"get",vendedor_admin)
        Assertions.assertEquals(true,resultado)
    }

    @Test
    fun testeMetodos(){
        val urlAcessada = "/clientes/666"
        Assertions.assertEquals(true,autorization.isAuthorized(urlAcessada,"post",vendedor_admin))
        Assertions.assertEquals(false,autorization.isAuthorized(urlAcessada,"patch",vendedor_admin))
    }

    @Test
    fun vendedorAdminFalse(){
        val urlAcessada = "/clientes/666/jose"
        val resultado = autorization.isAuthorized(urlAcessada,"get",vendedor_admin)
        Assertions.assertEquals(false,resultado)

        val resultado2 = autorization.isAuthorized(urlAcessada,"get",admin)
        Assertions.assertEquals(true,resultado2)
    }

    @Test
    fun vendedorEhVendedorAdmin(){
        val urlAcessada = "/vendedor"
        Assertions.assertEquals(true,autorization.isAuthorized("vendedor","get",vendedor_admin))
        Assertions.assertEquals(false,autorization.isAuthorized(urlAcessada,"get",vendedor))
        Assertions.assertEquals(true,autorization.isAuthorized("/vendedor","get",vendedor_admin))
        Assertions.assertEquals(true,autorization.isAuthorized(urlAcessada,"get",vendedor_admin))
        Assertions.assertEquals(true,autorization.isAuthorized(urlAcessada,"post",vendedor_admin))
    }

}