package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class VendaFuturaAbertoSqlTest {

    private fun view(nome: String) = Files.readString(Path.of("src/main/resources/views/$nome"))

    //Mesmo achatamento do Query.kt antes de subir para o SAP: tudo numa linha, espaco simples.
    private fun achatada(nome: String) = view(nome).replace(Regex("\\s+"), " ")

    private fun parametros(nome: String) = Regex(":(\\w+)").findAll(view(nome)).map { it.groupValues[1] }.toSet()

    @Test
    fun `ignora pedidos de venda futura legada`() {
        val sql = view("vendafutura-aberto.sql")

        Assertions.assertTrue(
            sql.contains("(\"ORDR\".\"U_legado_vf\" IS NULL OR \"ORDR\".\"U_legado_vf\" <> '1')")
        )
    }

    @Test
    fun `rotina de criacao de contrato le a v2`() {
        Assertions.assertEquals("vendafutura-aberto-v2.sql", VIEW_PEDIDOS_VENDA_FUTURA_ABERTOS)
    }

    @Test
    fun `v2 continua ignorando pedidos de venda futura legada`() {
        Assertions.assertTrue(
            view("vendafutura-aberto-v2.sql")
                .contains("(\"ORDR\".\"U_legado_vf\" IS NULL OR \"ORDR\".\"U_legado_vf\" <> '1')")
        )
    }

    /**
     * Pedido com NF gerada a partir dele seguiu o fluxo antigo de venda futura. Converter fecharia
     * o pedido ja faturado e abriria contrato para mercadoria ja entregue. NF cancelada nao conta:
     * o cancelamento devolve o saldo ao pedido.
     */
    @Test
    fun `v2 ignora pedido com NF nao cancelada gerada a partir dele`() {
        val sql = achatada("vendafutura-aberto-v2.sql")

        Assertions.assertTrue(sql.contains(
            "NOT EXISTS ( SELECT 1 FROM \"INV1\" nl INNER JOIN \"OINV\" n ON n.\"DocEntry\" = nl.\"DocEntry\" " +
            "WHERE nl.\"BaseType\" = 17 AND nl.\"BaseEntry\" = \"ORDR\".\"DocEntry\" AND n.\"CANCELED\" = 'N' )"
        ), sql)
    }

    /** Pedido -> entrega -> NF: a nota aponta para a entrega, entao o vinculo com o pedido esta na DLN1. */
    @Test
    fun `v2 ignora pedido com entrega nao cancelada gerada a partir dele`() {
        val sql = achatada("vendafutura-aberto-v2.sql")

        Assertions.assertTrue(sql.contains(
            "NOT EXISTS ( SELECT 1 FROM \"DLN1\" el INNER JOIN \"ODLN\" e ON e.\"DocEntry\" = el.\"DocEntry\" " +
            "WHERE el.\"BaseType\" = 17 AND el.\"BaseEntry\" = \"ORDR\".\"DocEntry\" AND e.\"CANCELED\" = 'N' )"
        ), sql)
    }

    @Test
    fun `v2 mantem os parametros da v1`() {
        Assertions.assertEquals(setOf("utilizacao", "startDate", "filiais"), parametros("vendafutura-aberto.sql"))
        Assertions.assertEquals(parametros("vendafutura-aberto.sql"), parametros("vendafutura-aberto-v2.sql"))
    }

    @Test
    fun `v2 nao tem comentario`() {
        val sql = view("vendafutura-aberto-v2.sql")
        Assertions.assertFalse(sql.contains("--"))
        Assertions.assertFalse(sql.contains("/*"))
    }
}
