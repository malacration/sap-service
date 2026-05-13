package br.andrew.sap.infrastructure.configurations.uzzipay

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.services.BussinessPlaceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UzziPayBusinessPlaceInitializerTests {

    @Test
    fun deveCarregarBusinessPlaceNasContasConfiguradas() {
        val conta = ContaUzziPayPix().also {
            it.idFilial = 1
            it.chavePix = "PIX-1"
            it.cnpj = "12345678000190"
            it.privateKey = "key"
            it.consulta = "consulta"
            it.contaContabilBanco = "contabil"
        }
        val env = UzziPayEnvrioment().also { it.contas = listOf(conta) }
        val bussinessPlaceService = mock<BussinessPlaceService>()
        whenever(bussinessPlaceService.getAllBusinessPlaces()).thenReturn(
            listOf(
                BussinessPlace().also {
                    it.BPLID = 1
                    it.BPLName = "Matriz"
                    it.Street = "Av. Rio Madeira"
                    it.City = "Porto Velho"
                    it.State = "RO"
                    it.ZipCode = "76.835-500"
                }
            )
        )

        UzziPayBusinessPlaceInitializer(env, bussinessPlaceService).loadBusinessPlaces()

        val businessPlace: BussinessPlace? = conta.businessPlace
        assertNotNull(businessPlace)
        assertEquals("Matriz", businessPlace?.BPLName)
        assertEquals("76835500", businessPlace?.ZipCode?.replace(Regex("\\D"), ""))
    }
}
