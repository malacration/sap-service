package br.andrew.sap.model.calculadora

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

/**
 * Custo da ultima compra que vai pra calculadora: NF de insumo na unidade de estoque mais o frete
 * bruto dos CTes que ela referencia no PCH21 (RefObjType 18), rateado pelo valor da mercadoria.
 */
class CustoUltimaCompraTest {

    private fun bd(v : String) = BigDecimal(v)

    //2 t na nota = 2000 kg no estoque
    private val nf = UltimaNfEntrada(DocEntry = 10, ItemCode = "INS01", LineTotal = bd("6000"), InvQty = bd("2000"))

    @Test
    fun `sem CTe o custo e o preco da nota na unidade de estoque`() {
        val custo = CustoUltimaCompra.calcular(nf, emptyList())
        assertEquals(0, bd("3").compareTo(custo.precoUnEstoque))
        assertEquals(0, BigDecimal.ZERO.compareTo(custo.frete))
        assertEquals(0, bd("3").compareTo(custo.custoComFreteUnEstoque))
    }

    @Test
    fun `CTe so desta nota rateia o frete entre os itens pelo valor`() {
        //nota de 10000 (este item 6000) e CTe de 500 -> item leva 300
        val ctes = listOf(CteDaNf(99, bd("500")) to listOf(NfDoCte(10, bd("10000"))))
        val custo = CustoUltimaCompra.calcular(nf, ctes)
        assertEquals(0, bd("300").compareTo(custo.frete))
        assertEquals(0, bd("0.15").compareTo(custo.freteUnEstoque))
        assertEquals(0, bd("3.15").compareTo(custo.custoComFreteUnEstoque))
    }

    @Test
    fun `CTe de varias notas divide o frete entre elas pelo valor`() {
        //CTe de 900 para duas notas: esta (10000) e outra (20000) -> esta leva 300, o item 6000/10000 disso
        val ctes = listOf(CteDaNf(99, bd("900")) to listOf(NfDoCte(10, bd("10000")), NfDoCte(11, bd("20000"))))
        val custo = CustoUltimaCompra.calcular(nf, ctes)
        assertEquals(0, bd("180").compareTo(custo.frete))
        assertEquals(0, bd("3.09").compareTo(custo.custoComFreteUnEstoque))
    }

    @Test
    fun `soma o frete de mais de um CTe`() {
        val ctes = listOf(
            CteDaNf(98, bd("200")) to listOf(NfDoCte(10, bd("10000"))),
            CteDaNf(99, bd("300")) to listOf(NfDoCte(10, bd("10000"))),
        )
        assertEquals(0, bd("300").compareTo(CustoUltimaCompra.calcular(nf, ctes).frete))
    }

    @Test
    fun `quantidade zero nao divide por zero`() {
        val custo = CustoUltimaCompra.calcular(UltimaNfEntrada(10, "INS01", bd("100"), BigDecimal.ZERO), emptyList())
        assertEquals(0, BigDecimal.ZERO.compareTo(custo.custoComFreteUnEstoque))
    }

    @Test
    fun `views do custo seguem o contrato com o SAP`() {
        val views = mapOf(
            "item-ultima-nf-entrada.sql" to listOf(":item"),
            "nf-entrada-ctes.sql" to listOf(":docEntry"),
            "cte-nfs-entrada.sql" to listOf(":cte"),
        )
        views.forEach { (arquivo, parametros) ->
            val sql = Files.readString(Path.of("src/main/resources/views/$arquivo"))
            assertFalse(sql.contains("--") || sql.contains("/*"), "$arquivo nao pode ter comentario")
            parametros.forEach { assertTrue(sql.contains(it), "$arquivo deve usar $it") }
            assertEquals(parametros.toSet(), Regex(":[a-zA-Z]+").findAll(sql).map { it.value }.toSet(), arquivo)
        }
        val ctes = Files.readString(Path.of("src/main/resources/views/nf-entrada-ctes.sql"))
        assertTrue(ctes.contains("\"PCH21\"") && ctes.contains("\"RefObjType\" = 18"))
        assertFalse(ctes.contains("PCH4"), "frete e o bruto: nao desconta os impostos do CTe")
    }
}
