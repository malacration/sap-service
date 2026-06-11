package br.andrew.sap.schedules.pix

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class DownPaymentPixExpirationSqlTest {

    @Test
    fun `nao seleciona adiantamentos de contrato de venda futura`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/down-payment-installment-pix-expirado.sql")
        )

        assertTrue(sql.contains("dpi.\"U_venda_futura\" IS NULL"))
    }
}

