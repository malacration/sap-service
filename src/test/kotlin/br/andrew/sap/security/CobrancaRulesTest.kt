package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RuleService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

/**
 * Le o rules.yml de verdade (RoleBasedAuthorizationFilterTest usa mock e nao veria isso). O
 * filtro e default-deny: endpoint novo sem regra responde 403 pra todo mundo menos admin - foi
 * exatamente o que aconteceu com o DELETE do historico de cobranca.
 */
class CobrancaRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RuleService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    private val urlDaLinha = "/cobranca/titulos/NF/500/1/historico/2"

    @Test
    fun `cobrador pode apagar linha do historico`() {
        assertTrue(autorizacao.isAuthorized(urlDaLinha, "delete", usuario("cobranca")))
    }

    @Test
    fun `cobrador segue lendo e registrando cobranca`() {
        assertTrue(autorizacao.isAuthorized("/cobranca/titulos", "get", usuario("cobranca")))
        assertTrue(autorizacao.isAuthorized("/cobranca/titulos/NF/500/1/acao", "post", usuario("cobranca")))
    }

    @Test
    fun `a permissao de delete nao vaza pro resto de cobranca`() {
        // A regra e do caminho da linha do historico, nao do /cobranca/** inteiro.
        assertFalse(autorizacao.isAuthorized("/cobranca/dominios", "delete", usuario("cobranca")))
        assertFalse(autorizacao.isAuthorized("/cobranca/titulos", "delete", usuario("cobranca")))
    }

    @Test
    fun `vendedor comum nao apaga historico - ele nem registra acao`() {
        assertFalse(autorizacao.isAuthorized(urlDaLinha, "delete", usuario("vendedor")))
        assertTrue(autorizacao.isAuthorized("/cobranca/titulos/NF/500/1/historico", "get", usuario("vendedor")))
    }
}
