package br.andrew.sap.services.documents

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * pedidos-listar.sql e o contrato da API antiga (a view e global no SAP); o filtro por DocNum
 * entrou na v2. "DocNum" < :docNumIsFilter: Int.MAX_VALUE libera tudo, -1 obriga o DocNum exato.
 */
class PedidosListarSqlTest {

    private val v1 = Files.readString(Path.of("src/main/resources/views/pedidos-listar.sql"))
    private val v2 = Files.readString(Path.of("src/main/resources/views/pedidos-listar-v2.sql"))

    private fun parametros(sql: String) = ":(\\w+)".toRegex().findAll(sql).map { it.groupValues[1] }.toSortedSet()

    @Test
    fun `v1 congelada e v2 so acrescenta o docNum`() {
        Assertions.assertEquals(
            sortedSetOf("cliente", "clienteIsFilter", "data", "filial", "filialIsFilter",
                "status", "statusIsFilter", "superVendedor", "vendedor"),
            parametros(v1))
        Assertions.assertEquals((parametros(v1) + "docNum" + "docNumIsFilter").toSortedSet(), parametros(v2))
        Assertions.assertTrue(v2.contains("(c.\"DocNum\" = :docNum OR c.\"DocNum\" < :docNumIsFilter)"))
    }

    @Test
    fun `sem comentario SQL`() {
        listOf(v1, v2).forEach {
            Assertions.assertFalse(it.contains("--"))
            Assertions.assertFalse(it.contains("/*"))
        }
    }
}
