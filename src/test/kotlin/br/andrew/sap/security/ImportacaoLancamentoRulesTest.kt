package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.RuleService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

/** Le o rules.yml de verdade, como o CobrancaRulesTest. */
class ImportacaoLancamentoRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RuleService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    private val contabil = usuario("contabil_importacao")

    @Test
    fun `perfil de importacao valida importa e ve o historico`() {
        assertTrue(autorizacao.isAuthorized("/journal/importacao/validar", "post", contabil))
        assertTrue(autorizacao.isAuthorized("/journal/importacao", "post", contabil))
        assertTrue(autorizacao.isAuthorized("/journal/importacao/historico", "get", contabil))
    }

    @Test
    fun `perfil de importacao nao alcanca o resto de journal`() {
        // /journal/save grava sem validar e o GET default-memo reescreve o historico do lancamento.
        assertFalse(autorizacao.isAuthorized("/journal/save", "post", contabil))
        assertFalse(autorizacao.isAuthorized("/journal/default-memo/10", "get", contabil))
        assertFalse(autorizacao.isAuthorized("/journal", "get", contabil))
        assertFalse(autorizacao.isAuthorized("/journal/importacao/historico", "delete", contabil))
    }

    @Test
    fun `outros perfis nao importam e admin importa`() {
        assertFalse(autorizacao.isAuthorized("/journal/importacao", "post", usuario("vendedor_admin", "cobranca")))
        assertTrue(autorizacao.isAuthorized("/journal/importacao", "post", usuario("admin")))
    }
}
