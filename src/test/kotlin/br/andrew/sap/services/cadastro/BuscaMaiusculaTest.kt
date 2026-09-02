package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * O HANA e case sensitive no LIKE e o normalizador de cadastro deixa CardName todo em caixa alta.
 * Sem subir o termo digitado junto, buscar "mauro" deixaria de achar "MAURO CARRETA" - a busca
 * quebraria justamente por causa da normalizacao.
 */
class BuscaMaiusculaTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val service = BusinessPartnersService(sqlQueriesService, mock(), mock(), mock())

    private val user = mock<User>().also {
        whenever(it.superVendedor()).doReturn(1)
        whenever(it.principal).doReturn(30)
    }

    private fun parametros(): List<Parameter> {
        val captor = argumentCaptor<List<Parameter>>()
        org.mockito.Mockito.verify(sqlQueriesService).execute(any<String>(), captor.capture())
        return captor.firstValue
    }

    private fun valorDaBusca() = parametros().single { it.toString().startsWith("valor=") }.toString()

    private fun respostaVazia() {
        whenever(sqlQueriesService.execute(any<String>(), any<List<Parameter>>()))
            .doReturn(OData(linkedMapOf("value" to "[]")))
    }

    @Test
    fun `busca de cliente sobe o termo para maiusculo`() {
        respostaVazia()

        service.fullSearchTextFallBack("mauro carreta", user)

        assertEquals("valor='%MAURO CARRETA%'", valorDaBusca())
    }

    @Test
    fun `busca de fornecedor sobe o termo para maiusculo`() {
        respostaVazia()

        service.searchBusinessPartners("frisacre")

        assertEquals("valor='%FRISACRE%'", valorDaBusca())
    }

    /** O curinga que o usuario digita continua valendo depois do uppercase. */
    @Test
    fun `mantem o curinga traduzido`() {
        respostaVazia()

        service.fullSearchTextFallBack("mauro*carreta", user)

        assertEquals("valor='%MAURO%CARRETA%'", valorDaBusca())
    }

    /** CPF/CNPJ e digito e pontuacao: nao passa pelo uppercase, so pela mascara. */
    @Test
    fun `cpf continua indo mascarado, sem uppercase`() {
        respostaVazia()

        service.fullSearchTextFallBack("12345678901", user)

        assertTrue(valorDaBusca().contains("."), valorDaBusca())
    }
}
