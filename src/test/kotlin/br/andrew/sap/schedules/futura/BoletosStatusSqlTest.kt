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

    // Status do boleto na tela: devolucao (nota de credito nao cancelada) vira "Cancelado (devolvido)",
    // recebimento (contas a receber nao cancelado) vira "Pago". GROUP BY + MAX para nao duplicar linhas:
    // o SQLQueries do Service Layer recusa subconsulta no SELECT ("Invalid SQL syntax", testado no HOMOLOG).
    @Test
    fun `devolucao e recebimento ignoram documentos cancelados`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/venda-futura/boletos-status.sql")
        )

        assertTrue(sql.contains("\"ORIN\".\"CANCELED\" = 'N'"))
        assertTrue(sql.contains("\"ORCT\".\"Canceled\" = 'N'"))
        assertTrue(sql.contains("as \"Recebimento\""))
        assertTrue(!sql.contains("DISTINCT"))
        assertTrue(!sql.contains("(SELECT"))
    }

    // PaidToDate do ODPI (SAP ja desconta recebimento cancelado) decide pago/parcial/aberto.
    // Nao pode virar SUM(RCT2.SumApplied): o join com RIN1 multiplica linhas e inflaria a soma.
    @Test
    fun `valor recebido vem do PaidToDate agrupado sem somar joins`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/venda-futura/boletos-status.sql")
        )

        assertTrue(sql.contains("\"ODPI\".\"PaidToDate\","))
        assertTrue(sql.contains("\"ODPI\".\"DocTotal\", \"ODPI\".\"PaidToDate\", \"ODPI\".\"DocStatus\""))
        assertTrue(!sql.contains("SUM("))
        // desempate estavel para paginacao por offset
        assertTrue(sql.trim().endsWith("ORDER BY \"ODPI\".\"DocDueDate\", \"ODPI\".\"DocEntry\""))
    }
}
