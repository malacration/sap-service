package br.andrew.sap.services.documents

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.stock.ItemsService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication

class DocumentForAngularTest {

    private val itemService = mock<ItemsService>()
    private val businessPartnersService = mock<BusinessPartnersService>()
    private val regiaoService = mock<RegiaoService>()
    private val auth = mock<Authentication>()
    private val service = DocumentForAngular()

    @Test
    fun `usa shipToCode do pedido para validar frete pelo endereco de entrega selecionado`() {
        val pedido = OrderSales(
            CardCode = "CLI001",
            DocDueDate = "2026-08-14",
            DocumentLines = listOf(Product("ITEM001", "10", "1")),
            BPL_IDAssignedToInvoice = "2",
        ).also {
            it.Incoterms = 1
            it.shipToCode = "FAZENDA"
            it.documentAdditionalExpenses.add(AdditionalExpenses.frete(100.0))
        }

        val cliente = BusinessPartner().also {
            it.setAddresses(
                listOf(
                    enderecoEntrega("ENTREGA", 10),
                    enderecoEntrega("FAZENDA", 20),
                )
            )
        }

        whenever(auth.principal).thenReturn("55")
        whenever(businessPartnersService.getById("'CLI001'")).thenReturn(odata(cliente))
        whenever(regiaoService.getRegioesByLocalidade("10")).thenReturn(listOf(regiao("10", distanciaKm = 100.0)))
        whenever(regiaoService.getRegioesByLocalidade("20")).thenReturn(listOf(regiao("20", distanciaKm = 1000.0)))

        service.prepareToSave(pedido, itemService, businessPartnersService, regiaoService, auth)
    }

    private fun enderecoEntrega(addressName: String, localidade: Int) = Address().also {
        it.addressType = AddresType.bo_ShipTo
        it.addressName = addressName
        it.U_Localidade = localidade
    }

    private fun regiao(localidade: String, distanciaKm: Double) = Regiao().also {
        it.Code = "REGIAO-$localidade"
        it.U_Ativa = "1"
        it.U_Filial = 2
        it.addLocalidade(localidade, distanciaKm)
        it.addFaixa(qtdeMinima = 1, valorKm = 1.0)
    }

    private fun odata(value: Any): OData {
        return OData(linkedMapOf("value" to OData().mapper.writeValueAsString(value)))
    }
}
