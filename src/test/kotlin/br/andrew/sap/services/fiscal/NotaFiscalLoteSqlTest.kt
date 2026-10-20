package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.NotaFiscalLoteFiltro
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

/**
 * Contrato do SQL com o validador do sap-odbc (ReadOnlySqlValidator): todo parametro do SQL
 * precisa ir no mapa e todo parametro do mapa precisa aparecer no SQL - sobra ou falta e
 * recusa com 400, e o erro so apareceria rodando contra o gateway.
 */
class NotaFiscalLoteSqlTest {

    private val sql = SqlResource.carregar("odbc/nota-fiscal-lote.sql")
    private val servico = NotaFiscalLoteService(mock(OdbcClient::class.java), mock(TaxNfeService::class.java),
        mock(BankPlusService::class.java), 200, 4)

    // Mesma regra do validador: ignora literal e identificador entre aspas e o `::` de cast.
    private fun parametrosDoSql(texto: String): Set<String> {
        val semAspas = texto.replace(Regex("'[^']*'"), "''").replace(Regex("\"[^\"]*\""), "\"\"")
        return Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(semAspas).map { it.groupValues[1] }.toSet()
    }

    @Test
    fun `listagem manda exatamente os parametros do SQL`() {
        assertEquals(parametrosDoSql(sql), servico.parametros(NotaFiscalLoteFiltro(), listOf(), listOf(), 0, 50).keys)
    }

    @Test
    fun `download manda exatamente os parametros do SQL`() {
        assertEquals(parametrosDoSql(sql), servico.parametros(NotaFiscalLoteFiltro(), listOf(1, 2), listOf(3), 0, 3).keys)
    }

    @Test
    fun `listagem vem por parcela, download e troca um por documento`() {
        assertEquals(1, servico.parametros(NotaFiscalLoteFiltro(), listOf(), listOf(), 0, 50)["porParcela"])
        assertEquals(0, servico.parametros(NotaFiscalLoteFiltro(), listOf(1), listOf(), 0, 1)["porParcela"])
        assertEquals(0, servico.parametros(NotaFiscalLoteFiltro(), listOf(), listOf(3), 0, 1)["porParcela"])
        // "adicionar todas do filtro": listagem por documento
        assertEquals(0, servico.parametros(NotaFiscalLoteFiltro(porDocumento = true), listOf(), listOf(), 0, 200)["porParcela"])
        // as duas pernas trazem a parcela e filtram vencimento por ela
        assertEquals(2, Regex("PA\\.\"InsTotal\" AS \"ValorParcela\"").findAll(sql).count())
        assertEquals(2, Regex("PA\\.\"DueDate\" >= :vencimentoDe").findAll(sql).count())
    }

    @Test
    fun `linha traz numero, valor e vencimento da parcela`() {
        val r = br.andrew.sap.model.fiscal.NotaFiscalResumo.from(mapOf("Tipo" to "NF", "DocEntry" to 10, "Parcelas" to 3,
            "Parcela" to 2, "ValorParcela" to "333.33", "VencimentoParcela" to "2026-11-20T00:00", "DocTotal" to "1000"))
        assertEquals(2, r.parcela)
        assertEquals("333.33".toBigDecimal(), r.valorParcela)
        assertEquals("2026-11-20", r.vencimentoParcela)
        assertEquals("1000".toBigDecimal(), r.total)
    }

    @Test
    fun `o que viaja nao tem comentario nem palavra proibida`() {
        assertFalse(sql.contains("--") || sql.contains("/*"))
        val proibidas = listOf("INSERT", "UPDATE", "DELETE", "MERGE", "UPSERT", "TRUNCATE", "DROP", "CREATE",
            "ALTER", "RENAME", "COMMENT", "GRANT", "REVOKE", "CALL", "EXEC", "EXECUTE", "PROCEDURE", "TRIGGER",
            "COMMIT", "ROLLBACK", "SAVEPOINT", "LOCK", "UNLOAD", "IMPORT", "EXPORT", "BACKUP", "CONNECT",
            "DISCONNECT", "SIGNAL", "INTO", "SHUTDOWN")
        proibidas.forEach { palavra ->
            assertFalse(Regex("\\b$palavra\\b", RegexOption.IGNORE_CASE).containsMatchIn(sql), palavra)
        }
        assertTrue(sql.startsWith("WITH"))
    }

    @Test
    fun `formas de pagamento nao tem parametro e so pega recebimento ativo`() {
        val formas = SqlResource.carregar("odbc/nota-fiscal-lote-formas-pagamento.sql")
        assertEquals(setOf<String>(), parametrosDoSql(formas))
        assertTrue(formas.contains("\"Type\" = 'I'") && formas.contains("\"Active\" = 'Y'"))
        assertFalse(formas.contains("--"))
    }

    @Test
    fun `consulta do cadastro do parceiro usa so forma e parceiros`() {
        val sqlCrd2 = SqlResource.carregar("odbc/nota-fiscal-lote-parceiros-com-forma.sql")
        assertEquals(setOf("forma", "parceiros"), parametrosDoSql(sqlCrd2))
        assertFalse(sqlCrd2.contains("--"))
    }

    @Test
    fun `adiantamento e so o de venda futura e sem o reconhecimento de devolucao`() {
        assertTrue(sql.contains("IFNULL(D.\"U_venda_futura\", 0) > 0"))
        assertTrue(sql.contains("IFNULL(D.\"U_TX_DocEntryRef\", 0) = 0"))
        assertTrue(sql.contains("D.\"DocEntry\" IN (:adiantamentos)"))
    }

    @Test
    fun `so entra nota emitida para a SEFAZ e nunca o documento de cancelamento`() {
        assertTrue(sql.contains("INNER JOIN \"Process\" P"))
        assertTrue(sql.contains("IFNULL(P.\"KeyNfe\", '') <> ''"))
        assertTrue(sql.contains("N.\"CANCELED\" <> 'C'"))
        assertTrue(sql.contains("N.\"Model\" = 39"))
    }
}
