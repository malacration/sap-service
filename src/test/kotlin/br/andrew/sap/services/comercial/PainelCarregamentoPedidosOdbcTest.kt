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

    private val sufixos = listOf("", "-por-cliente", "-por-item", "-por-vendedor", "-totais")
    private val base = Path.of("src/main/resources/odbc/painel-carregamento-linhas.sql")
    private val arquivos = listOf(base) + sufixos.map { Path.of("src/main/resources/odbc/painel-carregamento-pedidos$it.sql") }
    private val paginadas = sufixos.dropLast(1)

    private fun sqlCompleto(sufixo: String) =
        SqlResource.carregar("odbc/painel-carregamento-linhas.sql") + "\n" + SqlResource.carregar("odbc/painel-carregamento-pedidos$sufixo.sql")

    @Test
    fun `base filtra pela lista configurada e nao pela utilizacao 9 fixa`() {
        val sql = Files.readString(base)
        assertTrue(sql.contains("""L."Usage" IN (:utilizacoes)"""))
        assertFalse(Regex("""Usage"\s*=\s*9""").containsMatchIn(sql))
    }

    @Test
    fun `data sai em YYYYMMDD como o SQLQueries devolvia`() {
        assertTrue(Files.readString(base).contains("""TO_VARCHAR(P."DocDate", 'YYYYMMDD') AS "DocDate""""))
    }

    private fun params(vendedor: Int? = null) = listOf(
        Parameter("startDate", "2026-01-01"), Parameter("finalDate", "2026-12-31"),
        Parameter("partner", "%"), Parameter("search", "%"),
        Parameter("localidade", "%"), Parameter("incoterms", "%"),
    ) + PainelIntegradoVendasService.parametrosVendedor(vendedor)

    @Test
    fun `parametros levam a lista compartilhada de utilizacoes`() {
        val utilizacoes = CarregamentoUtilizacoes(listOf(9, null, 10)).lista
        assertEquals(listOf(9, 10), PainelIntegradoVendasService.parametrosOdbc(params(), listOf(2), utilizacoes, todos)["utilizacoes"])
    }

    @Test
    fun `filial vai como Int e vendedor sem filtro traz todos`() {
        val odbc = PainelIntegradoVendasService.parametrosOdbc(params(), listOf(2), listOf(9), todos)

        assertEquals(listOf(2), odbc["branch"])
        assertEquals(-2, odbc["vendedor"])
        assertEquals(-2, odbc["vendedorDisable"])
        assertFalse(odbc.containsKey("salesPerson"))
        assertEquals("2026-01-01", odbc["startDate"])
    }

    @Test
    fun `vendedor especifico filtra so ele`() {
        val odbc = PainelIntegradoVendasService.parametrosOdbc(params(12), listOf(2), listOf(9), todos)

        assertEquals(12, odbc["vendedor"])
        assertEquals(Int.MAX_VALUE, odbc["vendedorDisable"])
    }

    @Test
    fun `parametros de base mais cauda batem com as chaves do mapa para cada frete, tipo e emOrdem`() {
        // Mesma regra do ReadOnlySqlValidator do sap-odbc: nenhum ausente, nenhum sobrando.
        listOf("com", "sem", "todos").forEach { frete ->
            listOf("", "pedido", "nf-venda-futura").forEach { tipo ->
                listOf("todos", "sim", "nao").forEach { emOrdem ->
                    val chaves = PainelIntegradoVendasService.parametrosOdbc(params(), listOf(2), listOf(9),
                        CarregamentoService.sinaisFrete(frete, "todos")!!, tipo, emOrdem).keys
                    sufixos.forEach {
                        val usados = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(sqlCompleto(it)).map { m -> m.groupValues[1] }.toSet()
                        val esperadas = if (it == "-totais") chaves else chaves + setOf("inicio", "fim")
                        assertEquals(esperadas, usados, "painel-carregamento-pedidos$it.sql ($frete, $tipo, $emOrdem)")
                    }
                }
            }
        }
    }

    @Test
    fun `frete e utilizacoes so na perna de pedido, antes de qualquer agregacao`() {
        val sql = Files.readString(base)
        val pedido = sql.indexOf("FROM ORDR P")
        val faturas = sql.indexOf("FROM OINV P")
        val frete = sql.indexOf("""SIGN(IFNULL(P."TotalExpns", 0)) IN (:sinaisFrete)""")
        val usage = sql.indexOf("""L."Usage" IN (:utilizacoes)""")
        assertTrue(frete > pedido && usage > pedido, "frete/utilizacoes fora da perna de pedido")
        assertTrue(pedido > faturas, "perna de pedido deve vir depois da CTE FATURAS")
        assertFalse(sql.substring(faturas, pedido).let { it.contains(":sinaisFrete") || it.contains(":utilizacoes") })
    }

    @Test
    fun `cada cauda desempata a ordenacao pelo que identifica a linha`() {
        val lista = Files.readString(arquivos[1])
        assertTrue(lista.contains(""""DocDate" DESC, "Tipo", "DocEntry" DESC, "LineNum", "EmOrdemDeCarregamento""""))
        listOf("-por-cliente" to """"CardName", "CardCode"""", "-por-vendedor" to """"SlpName", "SlpCode"""", "-por-item" to """"ItemCode"""").forEach { (sufixo, chave) ->
            val sql = SqlResource.carregar("odbc/painel-carregamento-pedidos$sufixo.sql")
            assertTrue(sql.contains("DENSE_RANK() OVER (ORDER BY $chave)"), sufixo)
            assertTrue(sql.contains("""MAX("Linha") OVER () AS "Total""""), sufixo)
            val ordem = sql.substringAfterLast("ORDER BY")
            listOf("ItemCode", "Tipo", "EmOrdemDeCarregamento").forEach { assertTrue(ordem.contains("\"$it\""), "$sufixo ORDER BY sem $it") }
        }
    }

    @Test
    fun `estoque agrupado soma uma vez por deposito para pedido e NF`() {
        paginadas.drop(1).forEach {
            val sql = SqlResource.carregar("odbc/painel-carregamento-pedidos$it.sql")
            assertTrue(sql.contains("""GROUP BY""") && sql.contains("""MAX("OnHand")""") && sql.contains(""""WhsCode""""), it)
        }
        assertTrue(SqlResource.carregar("odbc/painel-carregamento-pedidos-por-item.sql").contains(""""EstoqueMinimo", "Tipo""""))
    }

    @Test
    fun `painel sem frete traz todos e valor invalido e recusado`() {
        listOf(null, "", " ", "todos").forEach { assertEquals(todos, CarregamentoService.sinaisFrete(it, "todos")) }
        assertEquals(listOf(1), CarregamentoService.sinaisFrete("com", "todos"))
        assertEquals(listOf(0), CarregamentoService.sinaisFrete("sem", "todos"))
        listOf("COM", "x").forEach { assertNull(CarregamentoService.sinaisFrete(it, "todos")) }
    }

    @Test
    fun `NF venda futura filtra pelo codigo exato do vendedor e so entra quando o tipo permite`() {
        val sql = Files.readString(base)
        assertEquals(2, Regex(Regex.escape("""(V."SlpCode" = :vendedor OR V."SlpCode" > :vendedorDisable)""")).findAll(sql).count())
        assertFalse(Regex("""SlpCode"\s+LIKE""").containsMatchIn(sql), "vendedor ainda por LIKE")
        assertTrue(sql.contains("""(:tipo = '' OR :tipo = 'nf-venda-futura')"""))
        assertTrue(sql.contains("""(:tipo = '' OR :tipo = 'pedido')"""))
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
