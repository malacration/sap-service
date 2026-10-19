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
 * Le o rules.yml de verdade. O lote baixa nota de qualquer cliente, entao nao pode vazar para o
 * business_partner (que so ve as proprias notas por /tax/**/pdf) nem para vendedor.
 */
class NotaFiscalLoteRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RegrasArquivoService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    @Test
    fun `perfil do lote lista e baixa`() {
        val u = usuario("nota_fiscal_lote")
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote", "get", u))
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote/download", "post", u))
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote/status-sefaz", "get", u))
        // troca de forma de pagamento: mesmo perfil (decisao do usuario)
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote/formas-pagamento", "get", u))
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote/forma-pagamento", "post", u))
    }

    @Test
    fun `perfil do lote alcanca os filtros da tela`() {
        val u = usuario("nota_fiscal_lote")
        assertTrue(autorizacao.isAuthorized("/branch", "get", u))
        assertTrue(autorizacao.isAuthorized("/nota-fiscal-lote/clientes/search", "post", u))
        assertTrue(autorizacao.isAuthorized("/sales-person/search", "post", u))
        // a busca de cliente e a da propria tela; a geral (recortada por carteira) nao e preciso
        assertFalse(autorizacao.isAuthorized("/business-partners/search", "post", u))
    }

    @Test
    fun `cliente e vendedor nao baixam em lote`() {
        listOf("business_partner", "vendedor", "vendedor_admin", "cobranca").forEach { perfil ->
            assertFalse(autorizacao.isAuthorized("/nota-fiscal-lote/download", "post", usuario(perfil)), perfil)
            assertFalse(autorizacao.isAuthorized("/nota-fiscal-lote", "get", usuario(perfil)), perfil)
            // busca sem recorte de carteira: vendedor nao pode usar para ver cliente de outro
            assertFalse(autorizacao.isAuthorized("/nota-fiscal-lote/clientes/search", "post", usuario(perfil)), perfil)
            assertFalse(autorizacao.isAuthorized("/nota-fiscal-lote/forma-pagamento", "post", usuario(perfil)), perfil)
        }
    }

    @Test
    fun `perfil do lote nao ganha o resto`() {
        val u = usuario("nota_fiscal_lote")
        assertFalse(autorizacao.isAuthorized("/invoice/10/create-pix", "get", u))
        assertFalse(autorizacao.isAuthorized("/business-partners/C001", "patch", u))
    }
}
