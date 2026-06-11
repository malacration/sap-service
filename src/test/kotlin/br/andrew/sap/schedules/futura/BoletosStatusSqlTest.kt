package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class BoletosStatusSqlTest {

    @Test
    fun `retorna identificacao sem campos lob no distinct`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/venda-futura/boletos-status.sql")
        )

        assertTrue(sql.contains("\"ODPI\".\"DocEntry\""))
        assertTrue(!sql.contains("\"DPI6\".\"U_QrCodePix\""))
        assertTrue(!sql.contains("\"DPI6\".\"U_pix_textContent\""))
    }
}
