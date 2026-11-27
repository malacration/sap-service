package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RegrasArquivoService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

/** Perfil da service account do Nota Agil: so a consulta de pagamento, nada de escrita. */
class ContasPagarRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RegrasArquivoService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("1", "Integracao", UserOriginEnum.EmployeesInfo, "nota-agil", "", "", listOf(), roles.toList())

    private val integracao = usuario("integracao_contas_pagar")

    @Test
    fun `integracao consulta pagamento de parcela`() {
        assertTrue(autorizacao.isAuthorized("/contas-pagar/pagamento-parcela/consulta", "post", integracao))
    }

    @Test
    fun `integracao nao alcanca nota de entrada pagamentos nem outros metodos`() {
        assertFalse(autorizacao.isAuthorized("/contas-pagar/pagamento-parcela/consulta", "get", integracao))
        assertFalse(autorizacao.isAuthorized("/nf-entrada/criar", "post", integracao))
        assertFalse(autorizacao.isAuthorized("/nf-entrada/cancel/1", "get", integracao))
        assertFalse(autorizacao.isAuthorized("/journal/save", "post", integracao))
    }

    @Test
    fun `vendedor nao consulta e admin consulta`() {
        assertFalse(autorizacao.isAuthorized("/contas-pagar/pagamento-parcela/consulta", "post", usuario("vendedor")))
        assertTrue(autorizacao.isAuthorized("/contas-pagar/pagamento-parcela/consulta", "post", usuario("admin")))
    }
}
