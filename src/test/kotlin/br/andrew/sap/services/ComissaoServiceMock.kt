package br.andrew.sap.services

import br.andrew.sap.model.Comissao
import br.andrew.sap.services.pricing.ComissaoService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

class ComissaoServiceMock {

    companion object{
        fun get(): ComissaoService {
            val mock = mock(ComissaoService::class.java)

            `when`(mock.getByIdTabela(5)).thenReturn(Comissao(1,5.5))
            `when`(mock.getByIdTabela(anyInt())).thenReturn(Comissao(1,5.5))
            return mock
        }
    }
}