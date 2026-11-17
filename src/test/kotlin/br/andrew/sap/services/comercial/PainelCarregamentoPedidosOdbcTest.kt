package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.comercial.PainelIntegradoVendas
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.logistica.CarregamentoUtilizacoes
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

/** Pedidos do painel integrado pelo sap-odbc com as mesmas utilizacoes da busca da ordem de carregamento. */
class PainelCarregamentoPedidosOdbcTest {

    private val todos = listOf(-1, 0, 1)

    private val arquivos = listOf("", "-por-cliente", "-por-item", "-por-vendedor")
        .map { Path.of("src/main/resources/odbc/painel-carregamento-pedidos$it.sql") }

    @Test
    fun `consultas filtram pela lista configurada e nao pela utilizacao 9 fixa`() {
        arquivos.forEach { arquivo ->
            val sql = Files.readString(arquivo)
            assertTrue(sql.contains("""L."Usage" IN (:utilizacoes)"""), "$arquivo sem IN (:utilizacoes)")
            assertFalse(Regex("""Usage"\s*=\s*9""").containsMatchIn(sql), "$arquivo ainda com Usage = 9")
        }
    }

    @Test
    fun `data sai em YYYYMMDD como o SQLQueries devolvia`() {
        assertTrue(Files.readString(arquivos.first()).contains("""TO_VARCHAR(P."DocDate", 'YYYYMMDD') AS "DocDate""""))
    }

    private fun params(vendedor: Int? = null) = listOf(
        Parameter("startDate", "2026-01-01"), Parameter("finalDate", "2026-12-31"), Parameter("branch", "2"),
        Parameter("partner", "%"), Parameter("search", "%"),
        Parameter("localidade", "%"), Parameter("incoterms", "%"),
    ) + PainelIntegradoVendasService.parametrosVendedor(vendedor)

    @Test
    fun `parametros levam a lista compartilhada de utilizacoes`() {
        val utilizacoes = CarregamentoUtilizacoes(listOf(9, null, 10)).lista
        assertEquals(listOf(9, 10), PainelIntegradoVendasService.parametrosOdbc(params(), 2, utilizacoes, todos)["utilizacoes"])
    }

    @Test
    fun `filial vai como Int e vendedor sem filtro traz todos`() {
        val odbc = PainelIntegradoVendasService.parametrosOdbc(params(), 2, listOf(9), todos)

        assertEquals(2, odbc["branch"])
        assertEquals(-2, odbc["vendedor"])
        assertEquals(-2, odbc["vendedorDisable"])
        assertFalse(odbc.containsKey("salesPerson"))
        assertEquals("2026-01-01", odbc["startDate"])
    }

    @Test
    fun `vendedor especifico filtra so ele`() {
        val odbc = PainelIntegradoVendasService.parametrosOdbc(params(12), 2, listOf(9), todos)

        assertEquals(12, odbc["vendedor"])
        assertEquals(Int.MAX_VALUE, odbc["vendedorDisable"])
    }

    @Test
    fun `parametros de cada sql batem com as chaves do mapa para cada frete`() {
        // Mesma regra do ReadOnlySqlValidator do sap-odbc: nenhum ausente, nenhum sobrando.
        listOf("com", "sem", "todos").forEach { frete ->
            val chaves = PainelIntegradoVendasService.parametrosOdbc(params(), 2, listOf(9),
                CarregamentoService.sinaisFrete(frete, "todos")!!).keys
            listOf("", "-por-cliente", "-por-item", "-por-vendedor").forEach {
                val sql = SqlResource.carregar("odbc/painel-carregamento-pedidos$it.sql")
                val usados = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(sql).map { m -> m.groupValues[1] }.toSet()
                assertEquals(chaves, usados, "painel-carregamento-pedidos$it.sql ($frete)")
            }
        }
    }

    @Test
    fun `consultas filtram frete pelo cabecalho do pedido antes de agrupar`() {
        arquivos.forEach { arquivo ->
            val sql = Files.readString(arquivo)
            val filtro = sql.indexOf("""AND SIGN(IFNULL(P."TotalExpns", 0)) IN (:sinaisFrete)""")
            assertTrue(filtro > sql.indexOf("FROM ORDR P"), "$arquivo sem filtro de frete no ORDR P")
            assertTrue(sql.indexOf("GROUP BY").let { it < 0 || filtro < it }, "$arquivo filtra depois do GROUP BY")
        }
    }

    @Test
    fun `painel sem frete traz todos e valor invalido e recusado`() {
        listOf(null, "", " ", "todos").forEach { assertEquals(todos, CarregamentoService.sinaisFrete(it, "todos")) }
        assertEquals(listOf(1), CarregamentoService.sinaisFrete("com", "todos"))
        assertEquals(listOf(0), CarregamentoService.sinaisFrete("sem", "todos"))
        listOf("COM", "x").forEach { assertNull(CarregamentoService.sinaisFrete(it, "todos")) }
    }

    @Test
    fun `NF venda futura filtra pelo codigo exato do vendedor`() {
        val sql = Files.readString(Path.of("src/main/resources/views/PainelInterativoCarregamento/faturas-reserva-carregamento.sql"))

        assertTrue(sql.contains("""(V."SlpCode" = :vendedor OR V."SlpCode" > :vendedorDisable)"""))
        assertFalse(Regex("""SlpCode"\s+LIKE""").containsMatchIn(sql), "vendedor ainda por LIKE")
        assertFalse(sql.contains("--") || sql.contains("/*"), "view nao pode ter comentario")
        val usados = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(sql).map { it.groupValues[1] }.toSet()
        assertEquals(params().map { it.key }.toSet(), usados)
    }

    @Test
    fun `linha do odbc vira PainelIntegradoVendas`() {
        val row = linkedMapOf<String, Any?>(
            "DocEntry" to 144095, "DocDate" to "20260929", "CardCode" to "CLI0002688", "CardName" to "PABLO",
            "SlpCode" to 12, "SlpName" to "Lucas", "ItemCode" to "PAC0000074", "Description" to "OX SUSTEN",
            "Usage" to 9, "DistribSum" to "234.5", "Quantity" to "30", "OnHand" to "95.5", "Incoterms" to "0",
            "Name" to null, "EmOrdemDeCarregamento" to null,
        )
        val linha = OData(row).tryGetValue<PainelIntegradoVendas>()

        assertEquals("20260929", linha.DocDate)
        assertEquals("12", linha.SlpCode)
        assertEquals(144095, linha.DocEntry)
        assertEquals(BigDecimal("30"), linha.Quantity)
        assertEquals(BigDecimal("95.5"), linha.OnHand)
    }
}
