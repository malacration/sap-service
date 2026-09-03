package br.andrew.sap.model.comercial

import br.andrew.sap.services.comercial.anularParesDeCancelamento
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * O SAP marca so um lado do estorno, entao o documento original ficava no mapa e continuava
 * somando nos totais. Os casos abaixo usam os numeros reais do contrato 396: as notas 72586/72588
 * (par cancelado, -1729,22 cada, conciliadas uma com a outra) e o adiantamento 4519, que tambem
 * fica conciliado nos dois sentidos com as notas de apropriacao - e NAO pode ser anulado junto.
 */
class MapaRelacoesCancelamentoTest {

    private fun nota(docEntry: Int, valor: String, situacao: String? = null) = MapaNode(
        id = "NOTA_FISCAL:$docEntry", tipo = MapaTipoDocumento.NOTA_FISCAL, docEntry = docEntry,
        docNum = docEntry.toString(), cardCode = "CLI0005670", label = "Nota Fiscal $docEntry",
        valor = BigDecimal(valor), data = null, status = "bost_Close", situacao = situacao
    )

    private fun adiantamento(docEntry: Int, valor: String, situacao: String? = null) = MapaNode(
        id = "ADIANTAMENTO:$docEntry", tipo = MapaTipoDocumento.ADIANTAMENTO, docEntry = docEntry,
        docNum = docEntry.toString(), cardCode = "CLI0005670", label = "Adiantamento $docEntry",
        valor = BigDecimal(valor), data = null, status = "bost_Close", situacao = situacao
    )

    /** As duas arestas que o SAP cria sozinho na ITR1 ao conciliar dois documentos. */
    private fun conciliacaoMutua(a: MapaNode, b: MapaNode) = listOf(
        MapaEdge("${a.id}->${b.id}", a.id, b.id, TipoAresta.CONCILIACAO),
        MapaEdge("${b.id}->${a.id}", b.id, a.id, TipoAresta.CONCILIACAO),
    )

    private fun situacaoDe(nodes: List<MapaNode>, id: String) = nodes.first { it.id == id }.situacao

    @Test
    fun `marca o original quando so o documento de cancelamento veio marcado`() {
        val original = nota(249485, "-1729.22")
        val cancelamento = nota(249487, "-1729.22", SituacaoNode.CANCELADO)

        val resultado = anularParesDeCancelamento(
            listOf(original, cancelamento),
            conciliacaoMutua(original, cancelamento)
        )

        Assertions.assertEquals(SituacaoNode.CANCELADO, situacaoDe(resultado, original.id))
        Assertions.assertEquals(SituacaoNode.CANCELADO, situacaoDe(resultado, cancelamento.id))
    }

    @Test
    fun `nao arrasta o adiantamento conciliado com uma nota cancelada`() {
        val notaCancelada = nota(249483, "-1729.22", SituacaoNode.CANCELADO)
        val adiantamento = adiantamento(10460, "-1729.22", SituacaoNode.PENDENTE_UTILIZACAO)

        val resultado = anularParesDeCancelamento(
            listOf(notaCancelada, adiantamento),
            conciliacaoMutua(notaCancelada, adiantamento)
        )

        Assertions.assertEquals(SituacaoNode.PENDENTE_UTILIZACAO, situacaoDe(resultado, adiantamento.id),
            "tipo diferente nao e par de cancelamento - o adiantamento continua valendo")
    }

    @Test
    fun `nao anula par de mesmo tipo com valores diferentes`() {
        val cancelada = nota(249487, "-1729.22", SituacaoNode.CANCELADO)
        val outra = nota(155407, "1729.22")

        val resultado = anularParesDeCancelamento(listOf(cancelada, outra), conciliacaoMutua(cancelada, outra))

        Assertions.assertNull(situacaoDe(resultado, outra.id),
            "valores que nao se anulam nao formam par de cancelamento")
    }

    @Test
    fun `nao anula nada quando nenhum lado veio cancelado`() {
        val a = nota(249485, "-1729.22")
        val b = nota(249487, "-1729.22")

        val resultado = anularParesDeCancelamento(listOf(a, b), conciliacaoMutua(a, b))

        Assertions.assertNull(situacaoDe(resultado, a.id))
        Assertions.assertNull(situacaoDe(resultado, b.id))
    }

    /** Conciliacao num sentido so (ex.: nota -> lancamento contabil) nao e cancelamento. */
    @Test
    fun `nao anula par ligado em um sentido so`() {
        val cancelada = nota(249487, "-1729.22", SituacaoNode.CANCELADO)
        val outra = nota(249485, "-1729.22")

        val resultado = anularParesDeCancelamento(
            listOf(cancelada, outra),
            listOf(MapaEdge("e1", cancelada.id, outra.id, TipoAresta.CONCILIACAO))
        )

        Assertions.assertNull(situacaoDe(resultado, outra.id))
    }
}
