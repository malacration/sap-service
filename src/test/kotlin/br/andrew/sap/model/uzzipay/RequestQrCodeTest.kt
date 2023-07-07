package br.andrew.sap.model.uzzipay

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RequestQrCodeTest {

    @Test
    fun decimalFormate(){
        //create a RequestQrCode
        val request = RequestQrCode(
            "externalIdentifier",
            "key",
            Type.EVP,
            12200.00.toBigDecimal(),
            "2024-12-31",
            Payer(
                "01847004261",
                "Andrew",
                "12345678",
                "123",
                "Rua dos Bobos",
                "São Paulo",
                "SP"
            ))

        Assertions.assertEquals("12200.00",request.amountFormat())

    }
}