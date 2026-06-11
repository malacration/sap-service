package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class VendaFuturaAbertoSqlTest {

    @Test
    fun `ignora pedidos de venda futura legada`() {
        val sql = Files.readString(Path.of("src/main/resources/views/vendafutura-aberto.sql"))

        Assertions.assertTrue(
            sql.contains("(\"ORDR\".\"U_legado_vf\" IS NULL OR \"ORDR\".\"U_legado_vf\" <> '1')")
        )
    }
}
