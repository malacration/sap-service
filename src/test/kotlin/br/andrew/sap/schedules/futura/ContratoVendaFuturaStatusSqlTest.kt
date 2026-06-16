package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class ContratoVendaFuturaStatusSqlTest {

    @Test
    fun `finalizacao financeira considera contratos abertos e entregues`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/contrato-vf/para-finalizar.sql")
        )

        assertTrue(sql.contains("vf.\"U_status\" IN ('aberto', 'entregue')"))
    }

    @Test
    fun `status entregue considera apenas contratos abertos com entrega`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/contrato-vf/entrega-finalizada.sql")
        )

        assertTrue(sql.contains("vf.\"U_status\" = 'aberto'"))
        assertTrue(sql.contains("nota.\"U_venda_futura\" = vf.\"DocEntry\""))
        assertTrue(sql.contains("nota.\"CANCELED\" = 'N'"))
    }
}
