package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.jwt.JwtAuthenticationFilter
import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.security.jwt.JwtSecretBean
import br.andrew.sap.model.authentication.User
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Com spring.security.disable=true nao existe login: o JwtAuthenticationFilter injeta um usuario
 * falso de SlpCode -1. Esse SlpCode nao existe em OSLP nem em @LIBERAPARA, entao sem o vinculo de
 * vendedor_admin a busca de produtos (order/produto-tabela.sql) volta vazia e nao da para vender.
 */
class BypassVendedorAdminTest {

    private val jwtHandler = JwtHandler(JwtSecretBean("segredo-de-teste-com-tamanho-suficiente-para-hmac"))

    private val filtro = JwtAuthenticationFilter(jwtHandler, true)

    private val produtoTabela = Files.readString(Path.of("src/main/resources/views/order/produto-tabela.sql"))

    @AfterEach
    fun limpa() = SecurityContextHolder.clearContext()

    private fun usuarioDoBypass(comToken : String? = null) : User {
        val request = MockHttpServletRequest("GET", "/item")
        if(comToken != null)
            request.addHeader("Authorization", comToken)
        filtro.doFilter(request, MockHttpServletResponse(), Mockito.mock(FilterChain::class.java))
        return SecurityContextHolder.getContext().authentication as User
    }

    /** Token interno valido de um business_partner - o que o /otp/login emite em modo disable. */
    private fun tokenDeBusinessPartner() : String {
        val cliente = User("12345678901", "Cliente", br.andrew.sap.model.authentication.UserOriginEnum.BusinessPartner,
            "", "", "", listOf(), listOf("business_partner"))
        return "Bearer " + jwtHandler.getToken(cliente).token
    }

    @Test
    fun `usuario do bypass entra como vendedor admin`() {
        val user = usuarioDoBypass()

        Assertions.assertTrue(user.roles.contains("vendedor_admin"))
        Assertions.assertTrue(user.isAdmin())
        Assertions.assertEquals(Int.MAX_VALUE, user.superVendedor())
    }

    /** superVendedor e o interruptor que libera todas as tabelas de preco no produto-tabela.sql. */
    @Test
    fun `vendedor comum continua limitado ao seu LIBERAPARA`() {
        val vendedor = User("42", "vendedor", br.andrew.sap.model.authentication.UserOriginEnum.SalePerson,
            "", "", "", listOf(), listOf("vendedor"))

        Assertions.assertEquals(-1, vendedor.superVendedor())
    }

    /**
     * A liberacao mora no proprio SQL: "PriceList" < :superVendedor e sempre verdadeiro com
     * Int.MAX_VALUE e sempre falso com -1 (ListNum nunca e negativo), mantendo a restricao do
     * @LIBERAPARA para vendedor comum. Mesmo idioma de contratos-vendafutura.sql.
     */
    /**
     * Em modo disable o token e ignorado por completo: o /otp/login aceita qualquer CPF/CNPJ sem
     * validar OTP e emite token de business_partner, que sem isso substituiria o admin-fake e
     * derrubaria o acesso a filiais e produtos.
     */
    @Test
    fun `token de business partner nao substitui o usuario do bypass`() {
        val user = usuarioDoBypass(tokenDeBusinessPartner())

        Assertions.assertEquals("-1", user.id)
        Assertions.assertTrue(user.roles.contains("vendedor_admin"))
        Assertions.assertEquals(Int.MAX_VALUE, user.superVendedor())
        Assertions.assertEquals(-1, user.getIdInt())
    }

    @Test
    fun `produto-tabela libera todas as tabelas para super vendedor`() {
        Assertions.assertTrue(produtoTabela.contains("\"ITM1\".\"PriceList\" < :superVendedor"),
            "sem essa clausula o vendedor_admin nao enxerga produto nenhum")
        Assertions.assertTrue(produtoTabela.contains("\"@LIBERAPARA\".\"U_vendedor\" = :vendedor"),
            "a restricao do vendedor comum tem que continuar valendo")
    }

    @Test
    fun `produto-tabela sem comentario SQL`() {
        Assertions.assertFalse(produtoTabela.contains("--"),
            "comentario quebra a view depois do achatamento em uma linha")
        Assertions.assertFalse(produtoTabela.contains("/*"), "comentario de bloco tambem nao e aceito")
    }
}
