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
 * Le o rules.yml de verdade. O filtro e default-deny, e a regra ampla de contrato de venda
 * futura so concede "get" - endpoint novo de escrita fica 403 sem uma regra propria.
 *
 * A troca recusa contrato antigo sem U_Localidade e manda o usuario atribuir no
 * PUT /contrato-venda-futura/{docEntry}/localidade. Sem essa permissao, quem pode trocar nao
 * conseguia cumprir o pre-requisito da propria troca sem chamar um administrador.
 */
class ContratoLocalidadeRulesTest {

    private val autorizacao = RoleBasedAuthorizationFilter(RuleService(DefaultResourceLoader()), "")

    private fun usuario(vararg roles: String) =
        User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), roles.toList())

    private val urlLocalidade = "/contrato-venda-futura/161/localidade"

    @Test
    fun `quem pode trocar pode atribuir a localidade`() {
        listOf("vendedor", "vendedor_admin").forEach { papel ->
            assertTrue(autorizacao.isAuthorized("/contrato-venda-futura/troca", "post", usuario(papel)),
                "$papel deveria poder trocar")
            assertTrue(autorizacao.isAuthorized(urlLocalidade, "put", usuario(papel)),
                "$papel pode trocar, entao precisa poder atribuir a localidade que a troca exige")
        }
    }

    @Test
    fun `admin continua podendo`() {
        assertTrue(autorizacao.isAuthorized(urlLocalidade, "put", usuario("admin")))
    }

    /** A permissao e do caminho da localidade, nao de escrita no contrato inteiro. */
    @Test
    fun `a permissao nao vaza para o resto do contrato`() {
        listOf("vendedor", "vendedor_admin").forEach { papel ->
            assertFalse(autorizacao.isAuthorized("/contrato-venda-futura/161", "put", usuario(papel)))
            assertFalse(autorizacao.isAuthorized("/contrato-venda-futura/161", "delete", usuario(papel)))
            assertFalse(autorizacao.isAuthorized(urlLocalidade, "delete", usuario(papel)))
        }
    }

    @Test
    fun `papel sem relacao com venda nao atribui localidade`() {
        assertFalse(autorizacao.isAuthorized(urlLocalidade, "put", usuario("cobranca")))
    }
}
