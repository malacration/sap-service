package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class BoletosOurNumberSqlTest {

    @Test
    fun `busca numero dos boletos pela entidade do bankplus`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/venda-futura/boletos-our-number.sql")
        )

        assertTrue(sql.contains("\"IV_IB_BillOfExchange\""))
        assertTrue(sql.contains("\"OurNumber\""))
        assertTrue(sql.contains("\"DocType\" = 203"))
        assertTrue(sql.contains("\"ODPI\".\"U_venda_futura\" = :idVendaFutura"))
    }
}
