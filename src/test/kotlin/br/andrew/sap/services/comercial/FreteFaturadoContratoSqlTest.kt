package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * A view devolve QUATRO linhas rotuladas, nunca um total ja somado: duas de ENTREGA
 * (nota de saida) e duas de DEVOLUCAO (nota de credito). Quem chama subtrai DEVOLUCAO
 * de ENTREGA.
 *
 * O total costurado dentro do SQL era o desenho original e ele nao subia: para inverter
 * o sinal da devolucao a query fazia `SUM(...) * -1`, e o parser do Service Layer recusa
 * a view inteira nesse `*` (erro 701, "no viable alternative at input"). Nenhuma das 88
 * views do projeto usa operador aritmetico - nao ha nenhuma prova de que o parser aceite
 * um. Por isso a inversao de sinal saiu do SQL e virou responsabilidade do Kotlin.
 */
class FreteFaturadoContratoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/venda-futura/frete-faturado-contrato.sql")
    )

    @Test
    fun `separa entrega de devolucao por rotulo em vez de somar no SQL`() {
        listOf("OINV", "INV3", "ORIN", "RIN3", ":idContrato").forEach {
            assertTrue(sql.contains(it), "consulta precisa conter $it")
        }
        assertEquals(2, Regex("'ENTREGA'").findAll(sql).count())
        assertEquals(2, Regex("'DEVOLUCAO'").findAll(sql).count())
        assertEquals(3, Regex("UNION ALL").findAll(sql).count())
    }

    @Test
    fun `nao usa operador aritmetico que o Service Layer recusa`() {
        assertFalse(Regex("""[)"\w]\s*[*/]\s*""").containsMatchIn(sql), "view nao pode multiplicar nem dividir")
        assertFalse(sql.contains("* -1"))
        assertFalse(sql.contains("FROM DUMMY"))
        assertFalse(sql.contains("FROM ("), "sem subquery derivada - construcao sem precedente nas views que sobem hoje")
    }

    @Test
    fun `so a entrega filtra pela flag de entrega do contrato`() {
        assertEquals(2, Regex("""N\."U_entrega_vf" = '1'""").findAll(sql).count())
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
    fun `ignora documentos cancelados`() {
        assertEquals(4, Regex("""N\."CANCELED" = 'N'""").findAll(sql).count())
    }

    @Test
    fun `arquivo nao contem comentarios SQL`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }
}
