package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RegrasArquivoService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

/**
 * Le o rules.yml de verdade. O papel `calculadora` libera a tela Relatorios > Calculadora sem dar
 * admin: o REST de /calculadora-preco e o pedido de produtos pelo WebSocket, que o
 * StompAuthChannelInterceptor checa como GET no destino.
 */
class CalculadoraRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RegrasArquivoService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    @Test
    fun `papel calculadora acessa a calculadora`() {
        val calculadora = usuario("calculadora")
        listOf(
            "/calculadora-preco/last-price/INS01/500.01",
            "/calculadora-preco/itens/all",
            "/calculadora-preco/product-trees-service/all",
            "/calculadora-preco/get-async",
            "/calculadora-preco",
        ).forEach { assertTrue(autorizacao.isAuthorized(it, "get", calculadora), it) }
    }

    @Test
    fun `papel calculadora nao ganha o resto do sistema`() {
        val calculadora = usuario("calculadora")
        listOf("/business-partners/search", "/pedido-venda/listar", "/cobranca/titulos", "/item/search")
            .forEach { assertFalse(autorizacao.isAuthorized(it, "get", calculadora), it) }
    }

    @Test
    fun `papel sem relacao com a calculadora continua sem acesso`() {
        assertFalse(autorizacao.isAuthorized("/calculadora-preco/last-price/INS01/500.01", "get", usuario("vendedor")))
    }
}
