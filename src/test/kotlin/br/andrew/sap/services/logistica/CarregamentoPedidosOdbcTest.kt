package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.OrderSales
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Linha como o sap-odbc devolve (decimal vem como texto, data com hora) virando OrderSales da busca. */
class CarregamentoPedidosOdbcTest {

    @Test
    fun `linha do odbc vira OrderSales da busca do carregamento`() {
        val row = linkedMapOf<String, Any?>(
            "DocEntry" to 144095, "DocNum" to 79234, "CardCode" to "CLI0002688", "CardName" to "PABLO",
            "DocDate" to "2026-09-29T00:00", "ItemCode" to "PAC0000074", "Dscription" to "OX SUSTEN",
            "Quantity" to "30", "Weight1" to "900", "U_LocalidadeS" to null, "CityS" to "LABREA",
            "BPL_IDAssignedToInvoice" to 2, "Name" to null, "OnHand" to "95.5", "WarehouseCode" to "500.01",
            "BaseLine" to 0, "DistribSum" to "234.5", "SlpName" to "Lucas", "Documento" to 1, "TotalDocumentos" to 33,
        )
        val pedido = OData(row).tryGetValue<OrderSales>()

        assertEquals("144095", pedido.DocEntry)
        assertEquals("LABREA", pedido.CityS)
        assertEquals(30.0, pedido.Quantity!!.toDouble())
        assertEquals(234.5, pedido.DistribSum)
        assertEquals(0, pedido.BaseLine!!.toInt())
    }
}
