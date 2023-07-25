package br.andrew.sap.services

import org.mockito.Mockito.*

class ItemServiceMock {

    companion object{
        fun get(): ItemsService {
            val mock = mock(ItemsService::class.java)

            `when`(mock.getPriceBase(anyString(), anyInt())).thenReturn(66.6)
            return mock
        }
    }
}