package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

/**
 * Cliente real com tres enderecos, dois deles chamados "COBRANCA" - um do tipo cobranca e um do
 * tipo entrega. A chave de endereco no SAP e (AddressName, AddressType); casar so pelo nome
 * escrevia a localidade sempre no primeiro da lista, o de cobranca.
 */
class AtualizaLocalidadeEnderecoTest {

    private val service = spy(BusinessPartnersService(mock(), mock(), mock(), mock())).also {
        doReturn(odata(cliente())).whenever(it).getById(any<String>())
        doReturn(null).whenever(it).update(any(), any<String>())
    }

    private fun cliente() = BusinessPartner().also {
        it.setAddresses(listOf(
            endereco("COBRANCA", AddresType.bo_BillTo, 10),
            endereco("ENTREGA", AddresType.bo_ShipTo, 20),
            endereco("COBRANCA", AddresType.bo_ShipTo, null)))
    }

    private fun endereco(nome: String, tipo: AddresType, localidade: Int?) = Address().also {
        it.addressName = nome
        it.addressType = tipo
        it.U_Localidade = localidade
    }

    private fun enderecosEnviados(): List<Address> {
        val captor = argumentCaptor<Any>()
        org.mockito.Mockito.verify(service).update(captor.capture(), any<String>())
        return (captor.firstValue as BusinessPartner).getAddresses()
    }

    @Test
    fun `grava no endereco de entrega, nao no de cobranca de mesmo nome`() {
        service.atualizaLocalidadeEndereco("CLI001", "COBRANCA", AddresType.bo_ShipTo, 99)

        val enviados = enderecosEnviados()
        assertEquals(99,
            enviados.single { it.addressName == "COBRANCA" && it.addressType == AddresType.bo_ShipTo }.U_Localidade,
            "a localidade tinha que ir para o endereco de ENTREGA chamado COBRANCA")
        assertEquals(10,
            enviados.single { it.addressType == AddresType.bo_BillTo }.U_Localidade,
            "o endereco de cobranca nao podia ser tocado")
    }

    @Test
    fun `grava no endereco de cobranca quando o tipo pedido e cobranca`() {
        service.atualizaLocalidadeEndereco("CLI001", "COBRANCA", AddresType.bo_BillTo, 77)

        val enviados = enderecosEnviados()
        assertEquals(77, enviados.single { it.addressType == AddresType.bo_BillTo }.U_Localidade)
        assertNull(
            enviados.single { it.addressName == "COBRANCA" && it.addressType == AddresType.bo_ShipTo }.U_Localidade,
            "o endereco de entrega de mesmo nome nao podia ser tocado")
    }

    @Test
    fun `nao mexe no outro endereco de entrega`() {
        service.atualizaLocalidadeEndereco("CLI001", "COBRANCA", AddresType.bo_ShipTo, 99)

        assertEquals(20, enderecosEnviados().single { it.addressName == "ENTREGA" }.U_Localidade)
    }

    @Test
    fun `erro nomeia o tipo quando o par nome+tipo nao existe`() {
        val erro = assertThrows<Exception> {
            service.atualizaLocalidadeEndereco("CLI001", "FAZENDA", AddresType.bo_ShipTo, 1)
        }

        assertTrue(erro.message!!.contains("FAZENDA"), erro.message)
        assertTrue(erro.message!!.contains("ENTREGA"), erro.message)
    }

    private fun odata(value: Any): OData {
        return OData(linkedMapOf("value" to OData().mapper.writeValueAsString(value)))
    }
}
