package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * A view existe para o detalhe do contrato: Filial, Vendedor e Numero do Pedido nao estao no
 * UDO "@AR_CONTRATO_FUTURO", so nos joins da listagem. Os aliases sao os nomes das
 * propriedades de [br.andrew.sap.model.self.vendafutura.Contrato] (Bplname com "n" minusculo,
 * como na contratos-vendafutura.sql) - renomear um alias apaga o campo na tela, sem erro.
 *
 * Os joins sao LEFT, ao contrario dos INNER da listagem: contrato com vendedor inativo ou
 * pedido de origem removido continua exibindo o que da para exibir, em vez de nao retornar
 * linha nenhuma e cair no contrato cru.
 */
class ContratoCabecalhoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/contrato-vf/contrato-cabecalho.sql")
    )

    @Test
    fun `expoe os aliases que o Contrato espera`() {
        listOf("\"SalesEmployeeName\"", "\"OrderDocNum\"", "\"Bplname\"").forEach {
            assertTrue(sql.contains("as $it"), "consulta precisa do alias $it")
        }
    }

    @Test
    fun `busca um contrato pelo DocEntry`() {
        assertTrue(sql.contains(":idContrato"))
        assertTrue(sql.contains("\"@AR_CONTRATO_FUTURO\".\"DocEntry\" = :idContrato"))
    }

    @Test
    fun `usa apenas join externo para nao perder a linha do contrato`() {
        assertEquals(3, Regex("LEFT JOIN").findAll(sql).count())
        assertFalse(sql.contains("INNER JOIN"))
    }

    @Test
    fun `nao repete o join de linhas que obrigaria agrupamento`() {
        assertFalse(sql.contains("AR_CF_LINHA"))
        assertFalse(sql.contains("group by", ignoreCase = true))
    }

    @Test
    fun `arquivo nao contem comentarios SQL`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }
}
