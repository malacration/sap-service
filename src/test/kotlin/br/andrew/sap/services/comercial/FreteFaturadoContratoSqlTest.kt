package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class FreteFaturadoContratoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/venda-futura/frete-faturado-contrato.sql")
    )

    @Test
    fun `soma notas de entrega e subtrai devolucoes do contrato`() {
        listOf("OINV", "INV3", "ORIN", "RIN3", ":idContrato").forEach {
            assertTrue(sql.contains(it), "consulta precisa conter $it")
        }
        assertTrue(sql.contains("N.\"U_entrega_vf\" = '1'"))
        assertTrue(sql.contains("SUM(E.\"U_frete_negociado\") * -1"))
        assertTrue(sql.contains("SUM(E.\"LineTotal\") * -1"))
    }

    @Test
    fun `usa apenas despesa de frete sem multiplicar pelas linhas de produto`() {
        assertTrue(sql.contains("E.\"ExpnsCode\" = 1"))
        assertFalse(sql.contains("INV1"))
        assertFalse(sql.contains("RIN1"))
    }

    @Test
    fun `prefere frete negociado e aceita documentos legados`() {
        assertTrue(sql.contains("E.\"U_frete_negociado\" > 0"))
        assertTrue(sql.contains("E.\"U_frete_negociado\" IS NULL"))
        assertTrue(sql.contains("E.\"U_frete_negociado\" <= 0"))
        assertTrue(sql.contains("SUM(E.\"LineTotal\")"))
    }

    @Test
    fun `ignora documentos cancelados e retorna zero sem movimentos`() {
        assertTrue(Regex("N\\.\\\"CANCELED\\\" = 'N'").findAll(sql).count() == 4)
        assertTrue(sql.contains("SELECT 0 AS \"Valor\" FROM DUMMY"))
    }

    @Test
    fun `arquivo nao contem comentarios SQL`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }
}
