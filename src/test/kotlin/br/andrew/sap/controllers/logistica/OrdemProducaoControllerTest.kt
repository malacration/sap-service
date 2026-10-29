package br.andrew.sap.controllers.logistica

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.logistica.ProductionOrdersService
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.comercial.ProductTreesService
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import br.andrew.sap.infrastructure.odata.Filter
import org.springframework.data.domain.Pageable

/**
 * O status da listagem vai direto pro $filter do OData, entao so passa o que esta no enum.
 */
class OrdemProducaoControllerTest {

    private val service = mock<ProductionOrdersService>()
    private val itemsService = mock<ItemsService>()
    private val productTreesService = mock<ProductTreesService>()
    private val controller = OrdemProducaoController(service, itemsService, productTreesService)

    @Test
    fun `sem status nao filtra`() {
        assertEquals("", controller.filtro(null).toString())
        assertEquals("", controller.filtro(listOf("", " ")).toString())
    }

    @Test
    fun `varios status viram um or no ProductionOrderStatus`() {
        assertEquals(
            "\$filter=(ProductionOrderStatus eq 'boposPlanned' or ProductionOrderStatus eq 'boposReleased')",
            controller.filtro(listOf("planejada", "LIBERADA")).toString()
        )
    }

    @Test
    fun `aceita o nome do SAP e ignora repetido`() {
        assertEquals(
            "\$filter=(ProductionOrderStatus eq 'boposClosed')",
            controller.filtro(listOf("boposClosed", "FECHADA")).toString()
        )
    }

    @Test
    fun `status fora do enum e recusado`() {
        assertThrows(IllegalArgumentException::class.java) {
            controller.filtro(listOf("boposPlanned' or 1 eq 1 or 'x"))
        }
    }

    @Test
    fun `detalhe traz bateladas e linhas e completa o nome que faltar`() {
        whenever(service.getById(10)).thenReturn(OData(linkedMapOf(
            "AbsoluteEntry" to 10,
            "DocumentNumber" to 77,
            "ItemNo" to "PAC*80",
            "PlannedQuantity" to 3600.0,
            "U_LbrOne_Batelada" to 2.0,
            "ProductionOrderLines" to listOf(
                mapOf("ItemNo" to "INS*1", "ItemName" to "CALCARIO", "ItemType" to "pit_Item", "PlannedQuantity" to 1000.0),
                mapOf("ItemNo" to "INS*2", "ItemType" to "pit_Item", "PlannedQuantity" to 500.0),
            )
        )))
        whenever(itemsService.get(any<Filter>(), any<Pageable>())).thenReturn(OData(linkedMapOf(
            "value" to listOf(mapOf("ItemCode" to "INS*2", "ItemName" to "SAL COMUM"))
        )))

        val ordem = controller.detalhe(10)

        assertEquals(2.0, ordem.U_LbrOne_Batelada)
        assertEquals(listOf("CALCARIO", "SAL COMUM"), ordem.ProductionOrderLines.map { it.ItemName })
        val filtro = argumentCaptor<Filter>()
        verify(itemsService).get(filtro.capture(), any<Pageable>())
        assertEquals("\$filter=(ItemCode eq 'INS*2')", filtro.firstValue.toString())
    }

    @Test
    fun `detalhe com todos os nomes nao consulta itens`() {
        whenever(service.getById(11)).thenReturn(OData(linkedMapOf(
            "AbsoluteEntry" to 11,
            "ProductionOrderLines" to listOf(mapOf("ItemNo" to "INS*1", "ItemName" to "CALCARIO"))
        )))

        controller.detalhe(11)

        verify(itemsService, never()).get(any<Filter>(), any<Pageable>())
    }

    @Test
    fun `cadastro do produto traz validade, peso do saco e formula do SysFeed`() {
        whenever(itemsService.getById("PAC0000167")).thenReturn(OData(linkedMapOf(
            "ItemCode" to "PAC0000167", "U_ValidadeProduto" to 90, "SalesUnitWeight" to 40.0, "SalesUnit" to "SACA 40KG")))
        whenever(productTreesService.getById("PAC0000167")).thenReturn(OData(linkedMapOf("U_LbrOne_Id" to 117)))

        val produto = controller.produto("PAC0000167")!!

        assertEquals(90, produto.ValidadeDias)
        assertEquals(40.0, produto.PesoSaco)
        assertEquals("SACA 40KG", produto.UnidadeVenda)
        assertEquals(117, produto.Formula)
    }

    @Test
    fun `sem estrutura a formula fica vazia e peso zero nao vira peso do saco`() {
        whenever(itemsService.getById("PDC0000002")).thenReturn(OData(linkedMapOf(
            "ItemCode" to "PDC0000002", "U_ValidadeProduto" to 365, "SalesUnitWeight" to 0.0)))
        whenever(productTreesService.getById("PDC0000002")).thenThrow(RuntimeException("sem estrutura"))

        val produto = controller.produto("PDC0000002")!!

        assertEquals(365, produto.ValidadeDias)
        assertNull(produto.PesoSaco)
        assertNull(produto.Formula)
    }

    @Test
    fun `cadastro indisponivel nao derruba o detalhe`() {
        whenever(service.getById(12)).thenReturn(OData(linkedMapOf(
            "AbsoluteEntry" to 12, "ItemNo" to "PDC0000002", "U_lote_producao" to "REP007568")))
        whenever(itemsService.getById("PDC0000002")).thenThrow(RuntimeException("fora do ar"))

        val ordem = controller.detalhe(12)

        assertNull(ordem.Produto)
        assertEquals("REP007568", ordem.U_lote_producao)
    }
}
