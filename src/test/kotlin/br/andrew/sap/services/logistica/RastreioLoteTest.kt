package br.andrew.sap.services.logistica

import br.andrew.sap.model.comercial.MapaRelacoesResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

/**
 * Cadeia baseada no hmg (lote S14007558):
 *   OP 30 gera o premix INS1/PR1
 *   OP 20 consome INS1/PR1 e gera o granel PDC1/PR2
 *   OP 10 consome PDC1/PR2 + o insumo comprado INS9/C1 e gera o ensacado PAC1/S1
 *   OP 40 (desmontagem) consome PAC1/S1 e devolve PDC1/PR3
 *   OP 99 consome o mesmo insumo comprado INS9/C1 - nao tem nada a ver com S1
 */
class RastreioLoteTest {

    private fun mov(ordem: Int, item: String, lote: String, papel: String, qtd: String, produto: String = item, tipo: String = "S") =
        MovimentoLote(ordem, 1000 + ordem, produto, "PRODUTO $produto", "L", tipo, "2026-10-28T00:00",
            item, "NOME $item", lote, papel, BigDecimal(qtd))

    private val base = listOf(
        mov(30, "INS1", "PR1", "P", "500"),
        mov(20, "INS1", "PR1", "C", "-500", produto = "PDC1"),
        mov(20, "PDC1", "PR2", "P", "4000"),
        mov(10, "PDC1", "PR2", "C", "-4000", produto = "PAC1"),
        mov(10, "INS9", "C1", "C", "-10", produto = "PAC1"),
        mov(10, "PAC1", "S1", "P", "100"),
        mov(40, "PAC1", "S1", "C", "-20", produto = "PAC1", tipo = "D"),
        mov(40, "PDC1", "PR3", "P", "800", produto = "PAC1", tipo = "D"),
        mov(99, "INS9", "C1", "C", "-5", produto = "PAC2"),
        mov(99, "PAC2", "S9", "P", "50"),
    )

    private val consultas = mutableListOf<BuscaLotes>()

    //mesmo contrato do SQL: todos os movimentos das OPs da busca, as `limite` mais novas
    private fun buscar(movimentos: List<MovimentoLote>) = { busca: BuscaLotes, limite: Int ->
        consultas += busca
        val ordens = movimentos
            .filter {
                (it.papel == "P" && (it.chave in busca.acima || it.lote == busca.loteGerado)) ||
                    (it.papel == "C" && (it.chave in busca.abaixo || it.lote == busca.loteConsumido))
            }
            .map { it.ordem }.distinct().sortedDescending().take(limite)
        movimentos.filter { it.ordem in ordens }
    }

    //S1 vendido em nota para o cliente CLI1 e em entrega para CLI2, com uma devolucao de nota
    private val vendasBase = listOf(
        VendaLote(13, 500, 9500, "2026-10-28T00:00", "CLI1", "CLIENTE UM", "PAC1", "S1", BigDecimal("-30")),
        VendaLote(15, 600, 9600, "2026-10-29T00:00", "CLI2", "CLIENTE DOIS", "PAC1", "S1", BigDecimal("-40")),
        VendaLote(14, 700, 9700, "2026-10-30T00:00", "CLI1", "CLIENTE UM", "PAC1", "S1", BigDecimal("5")),
    )

    private fun rastreio(movimentos: List<MovimentoLote> = base) =
        RastreioLote(buscar(movimentos), { lotes -> vendasBase.filter { it.chave in lotes } })

    private fun nivel(lote: String, item: String? = null, movimentos: List<MovimentoLote> = base) =
        rastreio(movimentos).nivel(lote, item)

    private fun MapaRelacoesResponse.ordens() = nodes.filter { it.id.startsWith("ORDEM_PRODUCAO:") }.mapNotNull { it.docEntry }.sorted()
    private fun MapaRelacoesResponse.no(id: String) = nodes.first { it.id == id }
    private fun MapaRelacoesResponse.aresta(de: String, para: String) = edges.firstOrNull { it.from == de && it.to == para }

    @Test
    fun `busca mostra so a OP que gerou o lote e os lotes que ela consumiu`() {
        val r = nivel("S1")

        assertEquals(listOf(10), r.ordens())
        assertEquals("LOTE:PAC1:S1", r.root)
        assertEquals(setOf("LOTE:PAC1:S1", "ORDEM_PRODUCAO:10", "LOTE:PDC1:PR2", "LOTE:INS9:C1"), r.nodes.map { it.id }.toSet())
        assertNull(r.aviso)
    }

    @Test
    fun `arestas vao do lote consumido para a OP e da OP para o lote gerado, com o numero do lote`() {
        val r = nivel("S1")

        assertEquals("PR2", r.aresta("LOTE:PDC1:PR2", "ORDEM_PRODUCAO:10")?.rotulo)
        assertEquals("C1", r.aresta("LOTE:INS9:C1", "ORDEM_PRODUCAO:10")?.rotulo)
        assertEquals("S1", r.aresta("ORDEM_PRODUCAO:10", "LOTE:PAC1:S1")?.rotulo)
        assertEquals(3, r.edges.size)
    }

    @Test
    fun `insumo produzido aqui abre no clique, insumo comprado nao`() {
        val r = nivel("S1")

        assertEquals(RastreioLote.ORIGEM_DISPONIVEL, r.no("LOTE:PDC1:PR2").situacao)
        assertEquals(RastreioLote.SEM_ORDEM_PRODUCAO, r.no("LOTE:INS9:C1").situacao)
        assertNull(r.no("LOTE:PAC1:S1").situacao)
        assertEquals("PDC1", r.no("LOTE:PDC1:PR2").item)
    }

    @Test
    fun `clique no lote abre o nivel seguinte pelo item e lote`() {
        val r = nivel("PR2", "PDC1")

        assertEquals(listOf(20), r.ordens())
        assertEquals("PR1", r.aresta("LOTE:INS1:PR1", "ORDEM_PRODUCAO:20")?.rotulo)
        assertEquals("PR2", r.aresta("ORDEM_PRODUCAO:20", "LOTE:PDC1:PR2")?.rotulo)
        assertEquals(RastreioLote.ORIGEM_DISPONIVEL, r.no("LOTE:INS1:PR1").situacao)
    }

    @Test
    fun `lote comprado aparece sozinho, com o item e o aviso`() {
        val r = nivel("C1")

        assertEquals(listOf("LOTE:INS9:C1"), r.nodes.map { it.id })
        assertEquals(listOf("INS9 - NOME INS9"), r.nodes.single().detalhes)
        assertEquals(RastreioLote.SEM_ORDEM_PRODUCAO, r.nodes.single().situacao)
        assertTrue(r.aviso!!.contains("não foi gerado por ordem de produção"), r.aviso)
        assertTrue(r.edges.isEmpty())
    }

    @Test
    fun `mesmo numero de lote em outro item - a busca mostra os dois, o clique so o do item`() {
        val movimentos = base + mov(77, "OUTRO", "PR2", "P", "1", produto = "OUTRO")

        assertEquals(listOf(20, 77), nivel("PR2", movimentos = movimentos).ordens())
        assertEquals(listOf(20), nivel("PR2", "PDC1", movimentos).ordens())
    }

    @Test
    fun `card da OP traz produto, desmontagem e lote gerado`() {
        val r = nivel("PR3")

        assertEquals(listOf("PAC1 - PRODUTO PAC1", "Desmontagem", "Gerou: PR3 (800)"), r.no("ORDEM_PRODUCAO:40").detalhes)
        assertEquals("1040", r.no("ORDEM_PRODUCAO:40").docNum)
        assertEquals("Fechada", r.no("ORDEM_PRODUCAO:40").status)
        assertEquals(RastreioLote.ORIGEM_DISPONIVEL, r.no("LOTE:PAC1:S1").situacao)
    }

    @Test
    fun `lote sem nenhuma OP explica`() {
        val erro = assertThrows<IllegalArgumentException> { nivel("NAOEXISTE") }

        assertTrue(erro.message!!.contains("NAOEXISTE"))
    }

    @Test
    fun `cada nivel faz no maximo duas consultas`() {
        nivel("S1")

        assertEquals(2, consultas.size)
        assertEquals("S1", consultas[0].loteGerado)
        assertEquals(setOf(LoteChave("PDC1", "PR2"), LoteChave("INS9", "C1")), consultas[1].acima)
    }

    @Test
    fun `todo lote abre o destino, inclusive o insumo comprado`() {
        val r = nivel("S1")

        assertEquals(true, r.no("LOTE:PAC1:S1").destino)
        assertEquals(true, r.no("LOTE:PDC1:PR2").destino)
        assertEquals(true, r.no("LOTE:INS9:C1").destino)
    }

    @Test
    fun `destino do insumo comprado mostra todas as OPs que o usaram`() {
        val r = rastreio().destino("C1", "INS9")

        assertEquals(listOf(10, 99), r.ordens())
        assertEquals("S9", r.aresta("ORDEM_PRODUCAO:99", "LOTE:PAC2:S9")?.rotulo)
    }

    @Test
    fun `destino mostra as OPs que consumiram o lote, o que elas geraram e quem comprou`() {
        val r = rastreio().destino("S1", "PAC1")

        assertEquals("LOTE:PAC1:S1", r.root)
        assertEquals(listOf(40), r.ordens())
        assertEquals("S1", r.aresta("LOTE:PAC1:S1", "ORDEM_PRODUCAO:40")?.rotulo)
        assertEquals("PR3", r.aresta("ORDEM_PRODUCAO:40", "LOTE:PDC1:PR3")?.rotulo)
        assertEquals(true, r.no("LOTE:PDC1:PR3").destino)

        val nota = r.no("NOTA_FISCAL:13:500")
        assertEquals(listOf("CLI1 - CLIENTE UM", "Saiu: 30"), nota.detalhes)
        assertEquals(500, nota.docEntry)
        assertEquals("S1", r.aresta("LOTE:PAC1:S1", "NOTA_FISCAL:13:500")?.rotulo)
        assertEquals(listOf("CLI2 - CLIENTE DOIS", "Saiu: 40"), r.no("ENTREGA:15:600").detalhes)
        assertNull(r.no("ENTREGA:15:600").docEntry)
        assertEquals(listOf("CLI1 - CLIENTE UM", "Voltou: 5"), r.no("DEVOLUCAO:14:700").detalhes)
        assertNull(r.aviso)
    }

    @Test
    fun `destino de lote que ainda esta em estoque avisa`() {
        val r = rastreio().destino("S9", "PAC2")

        assertEquals(listOf("LOTE:PAC2:S9"), r.nodes.map { it.id })
        assertTrue(r.aviso!!.contains("ainda não foi consumido"), r.aviso)
    }
}
