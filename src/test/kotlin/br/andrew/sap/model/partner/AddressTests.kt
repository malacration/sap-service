package br.andrew.sap.model.partner

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AddressTests {

    @Test
    fun cedilhaUper(){
        var address = Address().also { it.addressName = "COBRANÇA" }
        Assertions.assertEquals(false,address.isValid())
        address.addressName = address.normalize()
        Assertions.assertEquals("COBRANCA",address.addressName)
    }

    @Test
    fun cedilhaLow(){
        var address = Address().also { it.addressName = "COBRANçA" }
//        Assertions.assertEquals(false,address.isValid())
        address.addressName = address.normalize()
        Assertions.assertEquals("COBRANCA",address.addressName)
    }

    @Test
    fun acentoLow(){
        var address = Address().also { it.addressName = "á-à" }
        Assertions.assertEquals(false,address.isValid())
        address.addressName = address.normalize()
        Assertions.assertEquals("A-A",address.addressName)
    }


    @Test
    fun acentoUper(){
        var address = Address().also { it.addressName = "Á" }
        Assertions.assertEquals(false,address.isValid())
        address.addressName = address.normalize()
        Assertions.assertEquals("A",address.addressName)
    }
}