package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * SQL da conferencia de pagamento de nota de entrada, enviado ao sap-odbc. Contrato com o
 * ReadOnlySqlValidator de la: sem comentario no que viaja, uma instrucao SELECT, nenhuma palavra
 * proibida e exatamente os parametros que o gateway manda (falta ou sobra e 400).
 */
class ContasPagarSqlTest {

    private val fornecedor = SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_FORNECEDOR)
    private val parcelas = SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_PARCELAS)
    private val pagamentos = SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_PAGAMENTOS)
    private val notasNumero = SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_NOTAS_NUMERO)
    private val notasChave = SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_NOTAS_CHAVE)

    // Mesma regra do validador: ignora literal e identificador entre aspas e o `::` de cast.
    private fun semAspas(texto: String) = texto.replace(Regex("'[^']*'"), "''").replace(Regex("\"[^\"]*\""), "\"\"")

    private fun parametros(texto: String): Set<String> =
        Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(semAspas(texto)).map { it.groupValues[1] }.toSet()

    // Lista padrao de query.denied-keywords do sap-odbc (QueryProperties).
    private val proibidas = listOf(
        "INSERT", "UPDATE", "DELETE", "MERGE", "UPSERT", "TRUNCATE", "DROP", "CREATE", "ALTER", "RENAME", "COMMENT",
        "GRANT", "REVOKE", "CALL", "EXEC", "EXECUTE", "PROCEDURE", "TRIGGER", "COMMIT", "ROLLBACK", "SAVEPOINT", "LOCK",
        "UNLOAD", "IMPORT", "EXPORT", "BACKUP", "CONNECT", "DISCONNECT", "SIGNAL", "INTO", "SHUTDOWN",
    )

    @Test
    fun `somente SELECT, sem comentario, sem ponto e virgula e sem palavra proibida`() {
        for (sql in listOf(fornecedor, parcelas, pagamentos, notasNumero, notasChave)) {
            assertTrue(sql.startsWith("SELECT"))
            assertFalse(sql.contains("--"))
            assertFalse(sql.contains("/*"))
            assertFalse(sql.contains(";"))
            for (palavra in proibidas)
                assertFalse(Regex("\\b$palavra\\b", RegexOption.IGNORE_CASE).containsMatchIn(semAspas(sql)), palavra)
        }
    }

    @Test
    fun `parametros do SQL sao exatamente os enviados pelo gateway`() {
        assertEquals(parametros(fornecedor), ContasPagarSapGatewayImpl.parametrosFornecedor("12345678000190").keys)
        assertEquals(parametros(parcelas), ContasPagarSapGatewayImpl.parametrosParcelas(1).keys)
        assertEquals(parametros(pagamentos), ContasPagarSapGatewayImpl.parametrosPagamentos(1, 1).keys)
        assertEquals(parametros(notasNumero), ContasPagarSapGatewayImpl.parametrosNotasNumero(1, "F").keys)
        assertEquals(parametros(notasChave), ContasPagarSapGatewayImpl.parametrosNotasChave("1").keys)
    }

    @Test
    fun `notas de entrada nao canceladas com modelo e chave de acesso do TaxPlus`() {
        for (sql in listOf(notasNumero, notasChave)) {
            assertTrue(sql.contains("FROM OPCH N"))
            assertTrue(sql.contains("N.\"CANCELED\" = 'N'"), "cancelada (Y) e documento de cancelamento (C) nao contam")
            for (coluna in listOf("DocEntry", "DocNum", "Serial", "SeriesStr", "CardCode", "BPLId", "DocDate", "CANCELED",
                "Model", "U_ChaveAcesso"))
                assertTrue(sql.contains("AS \"$coluna\""), coluna)
            assertFalse(sql.contains("DocReceived"), "vinculo por DocEntry do TaxPlus e desatualizado")
        }
        assertTrue(notasNumero.contains("N.\"Serial\" = :numero"))
        assertTrue(notasNumero.contains("N.\"CardCode\" = :cardCode"))
        assertTrue(notasChave.contains("N.\"U_ChaveAcesso\" = :chave"))
    }

    @Test
    fun `fornecedor so parceiro tipo S e CNPJ comparado por digitos`() {
        assertTrue(fornecedor.contains("P.\"CardType\" = 'S'"))
        assertTrue(fornecedor.contains("REPLACE(REPLACE(REPLACE(REPLACE(D.\"TaxId0\", '.', ''), '/', ''), '-', ''), ' ', '') = :cnpj"))
    }

    @Test
    fun `parcelas da PCH6 com total, baixado, status e as duas moedas`() {
        assertTrue(parcelas.contains("FROM PCH6 P"))
        for (coluna in listOf("InstlmntID", "DueDate", "InsTotal", "PaidToDate", "Status", "DocCur", "MainCurncy"))
            assertTrue(parcelas.contains("AS \"$coluna\""), coluna)
        assertTrue(parcelas.contains("INNER JOIN OPCH N ON N.\"DocEntry\" = P.\"DocEntry\""))
        assertTrue(parcelas.contains("CROSS JOIN OADM A"))
    }

    @Test
    fun `parcelas trazem a retencao de categoria Pagamento da parcela e do documento`() {
        assertTrue(parcelas.contains("P.\"WTSum\" AS \"WTSum\""))
        assertTrue(parcelas.contains("(SELECT SUM(W.\"WTAmnt\") FROM PCH5 W"))
        assertTrue(parcelas.contains("W.\"AbsEntry\" = P.\"DocEntry\" AND W.\"Category\" = 'P') AS \"RetencaoPagamento\""),
            "so retencao de categoria Pagamento; a de Fatura ja esta fora do InsTotal")
    }

    @Test
    fun `pagamentos so de nota de entrada, da parcela pedida e sem pagamento cancelado`() {
        assertTrue(pagamentos.contains("INNER JOIN OVPM PR ON PR.\"DocEntry\" = PG.\"DocNum\""))
        assertTrue(pagamentos.contains("PG.\"InvType\" = 18"), "18 = nota fiscal de entrada (OPCH)")
        assertTrue(pagamentos.contains("PG.\"InstId\" = :parcela"))
        assertTrue(pagamentos.contains("PR.\"Canceled\" = 'N'"))
        assertTrue(pagamentos.contains("PG.\"SumApplied\" AS \"SumApplied\""))
        assertTrue(pagamentos.contains("PG.\"WtAppld\" AS \"WtAppld\""), "retencao descontada no pagamento")
    }

    /**
     * Tudo em `views/` e gravado no SAP como consulta global na subida (ProvisioningConfiguration),
     * sobrescrevendo outra de mesmo nome. A conferencia nao pode depender disso.
     */
    @Test
    fun `nada da conferencia fica em views, que e provisionado no SAP na subida`() {
        val views = PathMatchingResourcePatternResolver().getResources("classpath:/views/**/*.sql")
            .map { it.url.toString() }
        val antigas = listOf("fornecedor-por-documento.sql", "parcelas-nota-entrada.sql", "pagamentos-nota-entrada.sql")
        assertTrue(views.isNotEmpty())
        assertTrue(views.none { v -> v.contains("contas-pagar") || antigas.any { v.endsWith("/$it") } }, views.toString())
    }
}
