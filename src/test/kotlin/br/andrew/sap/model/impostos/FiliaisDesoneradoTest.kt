package br.andrew.sap.model.impostos

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Os schedules de desonerado puxavam documento de qualquer filial. O recorte e feito no filtro
 * OData, entao documento de filial fora da lista nem chega a ser lido do SAP.
 */
class FiliaisDesoneradoTest {

    private fun impostos(filiais: List<Int>) =
        ImpostosDesonerados(listOf(25), listOf(10, 28), filiais)

    @Test
    fun `filtro recorta pelas filiais configuradas`() {
        val filtro = Filter(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            impostos(listOf(2, 4, 11)).filtroFiliais()!!).toString()

        assertTrue(filtro.startsWith("\$filter=U_pedido_update eq '1' and ("), filtro)
        listOf(2, 4, 11).forEach {
            assertTrue(filtro.contains("BPL_IDAssignedToInvoice eq $it"), filtro)
        }
    }

    /** BPLId e Edm.Int32 no Service Layer: valor entre aspas faz o SAP recusar a query. */
    @Test
    fun `filial vai sem aspas`() {
        val render = impostos(listOf(2, 4)).filtroFiliais()!!.toString()

        assertTrue(render.contains("BPL_IDAssignedToInvoice eq 2"), render)
        assertTrue(!render.contains("'"), "BPLId e numerico, nao pode ir com aspas: $render")
    }

    /**
     * Lista vazia derruba a criacao do bean, entao a aplicacao nao sobe. Sem isso o filtro sairia
     * quebrado - o Condicao.IN renderiza "" e o Filter junta com " and ", virando "... and  and
     * ..." - e o schedule rodaria sem recorte de filial.
     */
    @Test
    fun `aplicacao nao sobe sem filial configurada`() {
        val erro = assertThrows(IllegalStateException::class.java) { impostos(listOf()) }

        assertTrue(erro.message!!.contains("imposto.icms.desonerado.filiais"), erro.message)
    }
}
