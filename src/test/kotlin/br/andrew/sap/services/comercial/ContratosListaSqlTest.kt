package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * A tela abre o contrato a partir da LINHA da listagem, nao do GET /{id}. Coluna que o contrato
 * precisa e nao esta aqui chega nula: foi assim que U_condicaoPagamento faltando fazia todo
 * contrato parecer legado e pedir sanitizacao a cada troca.
 *
 * O nome da condicao vem por LEFT JOIN: contrato sem condicao (legado) ou a vista (-1, que pode
 * nao ter linha no OCTG) nao pode sumir da listagem.
 */
class ContratosListaSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/contrato-vf/contratos-vendafutura.sql"))

    @Test
    fun `traz a condicao de pagamento do contrato e o nome dela`() {
        assertTrue(sql.contains("\"@AR_CONTRATO_FUTURO\".\"U_condicaoPagamento\","))
        assertTrue(sql.contains("\"OCTG\".\"PymntGroup\" as \"CondicaoPagamentoNome\""))
        assertTrue(sql.contains("LEFT JOIN \"OCTG\""))
    }

    @Test
    fun `colunas novas tambem no group by`() {
        val groupBy = sql.substringAfter("group by")
        assertTrue(groupBy.contains("\"@AR_CONTRATO_FUTURO\".\"U_condicaoPagamento\""))
        assertTrue(groupBy.contains("\"OCTG\".\"PymntGroup\""))
    }

    @Test
    fun `sem comentario SQL`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }
}
