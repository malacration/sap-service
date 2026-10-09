package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Regras da view de condicoes usada na troca de contrato de venda futura (comentario dentro do
 * .sql quebra o provisionamento, ver views/README.md). A view da tela de venda,
 * prazo-por-tabela-preco.sql, continua com o filtro de U_Rov_EnviarForca e o INNER JOIN.
 */
class PrazoContratoSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/order/prazo-contrato-por-tabela-preco.sql"))
    private val maiuscula = sql.uppercase()

    @Test
    fun `sem comentario SQL nem construcao rejeitada pelo SQLQueries`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
        listOf("CASE", "IFNULL", "COALESCE", "CAST(", "UNION").forEach {
            assertFalse(maiuscula.contains(it), "a view usa '$it', sem precedente nas views do projeto")
        }
    }

    @Test
    fun `condicao do contrato vale mesmo sem estar liberada para venda nova`() {
        assertFalse(maiuscula.contains("ENVIARFORCA"))
    }

    @Test
    fun `condicao a vista sem linha no OCTG nao some do resultado`() {
        assertTrue(maiuscula.contains("LEFT JOIN \"OCTG\""))
        assertTrue(maiuscula.contains("COND.\"U_PRAZO\" AS \"GROUPNUM\""), "GroupNum vem da condicao, nao do OCTG")
    }
}
