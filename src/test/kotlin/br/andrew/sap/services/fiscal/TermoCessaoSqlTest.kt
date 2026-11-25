package br.andrew.sap.services.fiscal

import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TermoCessaoSqlTest {
    // Sem comentarios (SqlResource.carregar os remove): so o SQL que roda.
    private val sql = SqlResource.carregar("odbc/nota-fiscal-lote-termo-cessao.sql")

    @Test
    fun `termo cobre NF aberta (O) e entregue (D), nunca fechada, e exclui cancelada`() {
        val predicados = Regex("N\\.\"DocStatus\"\\s*(=|IN)\\s*(\\([^)]*\\)|'[^']*')").findAll(sql).map { it.groupValues[2] }.toList()
        assertEquals(listOf("('O','D')"), predicados)
        assertTrue(sql.contains("N.\"CANCELED\" = 'N'"))
    }
}
