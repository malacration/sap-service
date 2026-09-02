package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.*
import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

/**
 * O Service Layer responde "Internal error (-5002)" quando o corpo do PATCH traz propriedade que
 * nao existe na entidade do SAP (foi assim com o TaxCode da despesa adicional, ver
 * [AdditionalExpensesJsonTest]). Como o payload e montado por reflexao do Jackson, qualquer getter
 * novo sem @JsonIgnore vaza um campo inventado para dentro do documento sem ninguem perceber -
 * o SalesOrderCalculaDesoneradoSchedule so descobre no 400.
 *
 * Esse teste serializa com o mesmo ObjectMapper que o RestTemplate usa e trava o payload.
 */
class PatchPayloadDoDocumentoTest {

    private val mapper = MappingJackson2HttpMessageConverter().objectMapper

    private fun pedido(): Document {
        val linha = Product("PAC0000069", "1.0", "124.2236", 9).also {
            it.LineNum = 0
            it.TaxCode = "5101-025"
            it.U_preco_negociado = 100.0
            it.CFOPCode = "5101"
            it.LineTotal = 78.22
            //linha real vem do SAP com os impostos preenchidos - montar a linha sem eles
            //esconde justamente o campo que derrubou o PATCH do DocNum 65581
            it.LineTaxJurisdictions = listOf(
                LineTaxJurisdiction().also { j ->
                    j.JurisdictionType = 25; j.JurisdictionCode = "IC17BT14"
                    j.TaxAmount = 15.25; j.TaxRate = 19.5
                })
        }
        return Document("CLI0003130", "2026-08-25T00:00:00Z", listOf(linha), "2").also {
            it.docEntry = 118927
            it.docNum = "65581"
            it.u_pedido_update = "0"
            it.documentAdditionalExpenses = mutableListOf(AdditionalExpenses.frete(1500.0))
            it.TaxExtension = TaxExtension().also { t -> t.Incoterms = 9 }
            it.docObjectCode = DocumentTypes.oOrders
            it.DocumentStatus = DocumentStatus.bost_Open
        }
    }

    private fun campos(node: JsonNode) = node.fieldNames().asSequence().toList()

    /** getOrCreateTaxExtension() e um getter: sem @JsonIgnore o Jackson publica esse campo. */
    @Test
    fun `documento nao envia OrCreateTaxExtension`() {
        val json = mapper.writeValueAsString(pedido())

        assertFalse(json.contains("OrCreateTaxExtension"),
            "campo inexistente no SAP quebra o PATCH com -5002: $json")
        assertTrue(json.contains("\"TaxExtension\""))
    }

    /** @JsonProperty so na propriedade deixa o getter criar um "Cfopcode" duplicado. */
    @Test
    fun `linha envia CFOPCode uma vez so`() {
        val linha = mapper.readTree(mapper.writeValueAsString(pedido())).get("DocumentLines").get(0)

        assertTrue(campos(linha).contains("CFOPCode"))
        assertFalse(campos(linha).contains("Cfopcode"),
            "duplicata gerada pelo getter: ${campos(linha)}")
    }

    /** O SAP calcula os impostos da linha; devolve-los num PATCH derruba com -5002. */
    @Test
    fun `linha nao envia LineTaxJurisdictions`() {
        val json = mapper.writeValueAsString(pedido())

        assertFalse(json.contains("LineTaxJurisdictions", ignoreCase = true),
            "imposto calculado pelo SAP nao volta no PATCH: $json")
        assertFalse(json.contains("IC17BT14"))
    }

    /**
     * Rede de seguranca: todo campo do corpo tem que ser propriedade conhecida do SAP. Qualquer
     * getter/propriedade nova entra aqui de proposito, e nao de surpresa num 400 do schedule.
     */
    @Test
    fun `payload so tem propriedades conhecidas do SAP`() {
        val conhecidos = setOf(
            "CardCode", "DocDueDate", "DocumentLines", "BPL_IDAssignedToInvoice", "Comments",
            "DocDate", "SalesPersonCode", "PaymentGroupCode", "DocEntry", "DocNum", "PaymentMethod",
            "DocumentInstallments", "JournalMemo", "Cancelled", "U_pedido_update", "DocTotal",
            "DiscountPercent", "TotalDiscount", "ReserveInvoice", "SequenceSerial", "SequenceModel",
            "CreateDate", "SeriesString", "U_ChaveAcesso", "DflWhs", "U_faturadoOrdemCarregamento",
            "U_id_pedido_forca", "U_uuid_forca", "CardName", "OpeningRemarks", "ControlAccount",
            "Model", "DocObjectCode", "AttachmentEntry", "DocumentStatus",
            "DocumentAdditionalExpenses", "AddressExtension", "ShipToCode", "Address", "Address2",
            "U_assinatura", "U_rd_station", "U_venda_futura", "U_entrega_vf", "U_vf_estornada",
            "U_legado_vf", "DownPaymentsToDraw", "TransNum", "SequenceCode", "U_TX_DocEntryRef",
            "U_TX_DocTypeRef", "ClosingRemarks", "TaxExtension", "VehicleState", "Incoterms")

        val corpo = mapper.readTree(mapper.writeValueAsString(pedido()))

        val desconhecidos = campos(corpo).filterNot { conhecidos.contains(it) }
        assertTrue(desconhecidos.isEmpty(),
            "campo que o SAP nao conhece derruba o PATCH com -5002: $desconhecidos")

        val conhecidosLinha = setOf(
            "ItemCode", "Quantity", "UnitPrice", "Usage", "DocEntry", "ItemDescription",
            "CommisionPercent", "U_idTabela", "LineNum", "TaxCode", "DiscountPercent",
            "U_preco_base", "U_preco_negociado", "WarehouseCode", "U_id_item_forca",
            "CostingCode", "CostingCode2", "AccountCode", "MeasureUnit", "PriceList",
            "ListName", "OnHand", "LineTotal", "PriceUnit", "U_LBR_Destinacao",
            "U_ORD_CARREGAMENTO", "BaseType", "BaseEntry", "BaseLine", "SalUnitMsr",
            "FatherType", "BatchNumbers", "CFOPCode", "LineTotalDesonerado")

        val desconhecidosLinha = campos(corpo.get("DocumentLines").get(0))
            .filterNot { conhecidosLinha.contains(it) }
        assertTrue(desconhecidosLinha.isEmpty(),
            "campo que o SAP nao conhece na linha derruba o PATCH com -5002: $desconhecidosLinha")
    }
}
