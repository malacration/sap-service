package br.andrew.sap.services.logistica

import br.andrew.sap.model.comercial.MapaEdge
import br.andrew.sap.model.comercial.MapaNode
import br.andrew.sap.model.comercial.MapaRelacoesResponse
import br.andrew.sap.model.comercial.MapaTipoDocumento
import br.andrew.sap.model.comercial.TipoAresta
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Rastreio de lote pelas ordens de producao, no formato do mapa de relacoes, um nivel por vez, nos dois
 * sentidos a partir do lote buscado:
 *  - origem (nivel): a OP que gerou o lote e os lotes que ela consumiu. Lote consumido que tambem foi
 *    produzido aqui vem marcado ORIGEM_DISPONIVEL e a tela pede o nivel dele no clique; lote comprado vem
 *    marcado SEM_ORDEM_PRODUCAO e para ali.
 *  - destino: as OPs que consumiram o lote, os lotes que elas geraram (que abrem o destino de novo) e os
 *    documentos de venda com o cliente - quem comprou aquele lote.
 *
 * Duas idas ao HANA por nivel (~0,3s cada): o proxy na frente da API corta requisicao com mais de ~1,6s.
 */
@Service
class RastreioLoteService(private val odbc: OdbcClient) {

    private val sql by lazy { SqlResource.carregar("odbc/rastreio-lote-movimentos.sql") }
    private val sqlVendas by lazy { SqlResource.carregar("odbc/rastreio-lote-vendas.sql") }

    /** @param item vazio na busca digitada (o lote pode existir em mais de um item); preenchido no clique. */
    fun nivel(lote: String, item: String?): MapaRelacoesResponse =
        RastreioLote(::consultar, ::vendas).nivel(lote, item)

    fun destino(lote: String, item: String): MapaRelacoesResponse =
        RastreioLote(::consultar, ::vendas).destino(lote, item)

    private fun vendas(lotes: Set<LoteChave>): List<VendaLote> {
        val params = mapOf(
            "lotes" to lotes.map { "${it.item}|${it.lote}" }.ifEmpty { listOf("") },
            "textos" to lotes.map { it.lote }.distinct().ifEmpty { listOf("") },
        )
        return odbc.consultar(sqlVendas, params, MAX_LINHAS).rows.map(VendaLote::from)
    }

    private fun consultar(busca: BuscaLotes, limite: Int): List<MovimentoLote> {
        val params = mapOf(
            "loteGerado" to busca.loteGerado,
            "loteConsumido" to busca.loteConsumido,
            "lotesAcima" to busca.acima.map { "${it.item}|${it.lote}" }.ifEmpty { listOf("") },
            "textosAcima" to busca.acima.map { it.lote }.distinct().ifEmpty { listOf("") },
            "lotesAbaixo" to busca.abaixo.map { "${it.item}|${it.lote}" }.ifEmpty { listOf("") },
            "textosAbaixo" to busca.abaixo.map { it.lote }.distinct().ifEmpty { listOf("") },
            "limite" to limite,
        )
        return odbc.consultar(sql, params, MAX_LINHAS).rows.map(MovimentoLote::from)
    }

    companion object {
        //teto do sap-odbc; com no maximo MAX_ORDENS OPs por consulta cabe com folga
        const val MAX_LINHAS = 10000
    }
}

/** O que uma OP fez com um lote, ja liquido de estorno e devolucao (ver rastreio-lote-movimentos.sql). */
data class MovimentoLote(
    val ordem: Int,
    val ordemNumero: Int?,
    val ordemItem: String,
    val ordemItemNome: String?,
    val ordemStatus: String?,
    val ordemTipo: String?,
    val ordemData: String?,
    val item: String,
    val itemNome: String?,
    val lote: String,
    //P = gerado pela OP, C = consumido
    val papel: String,
    val quantidade: BigDecimal,
) {
    //o numero do lote so e unico junto do item
    val chave get() = LoteChave(item, lote)
    val gerou get() = papel == "P" && quantidade.signum() > 0
    val consumiu get() = papel == "C" && quantidade.signum() < 0

    companion object {
        fun from(linha: Map<String, Any?>) = MovimentoLote(
            ordem = inteiro(linha["Ordem"])!!,
            ordemNumero = inteiro(linha["OrdemNumero"]),
            ordemItem = linha["OrdemItem"].toString(),
            ordemItemNome = linha["OrdemItemNome"]?.toString(),
            ordemStatus = linha["OrdemStatus"]?.toString(),
            ordemTipo = linha["OrdemTipo"]?.toString(),
            ordemData = linha["OrdemData"]?.toString(),
            item = linha["Item"].toString(),
            itemNome = linha["ItemNome"]?.toString(),
            lote = linha["Lote"].toString(),
            papel = linha["Papel"].toString(),
            quantidade = BigDecimal(linha["Quantidade"].toString()),
        )

        private fun inteiro(valor: Any?): Int? = valor?.toString()?.toBigDecimalOrNull()?.toInt()
    }
}

data class LoteChave(val item: String, val lote: String)

/** Documento de venda (ou devolucao) que mexeu num lote - ver rastreio-lote-vendas.sql. */
data class VendaLote(
    //OITL.DocType: 15 entrega, 13 nota fiscal, 16 devolucao, 14 devolucao de nota
    val tipo: Int,
    val docEntry: Int,
    val docNum: Int?,
    val data: String?,
    val cliente: String?,
    val clienteNome: String?,
    val item: String,
    val lote: String,
    //negativa: saiu para o cliente; positiva: voltou
    val quantidade: BigDecimal,
) {
    val chave get() = LoteChave(item, lote)

    companion object {
        fun from(linha: Map<String, Any?>) = VendaLote(
            tipo = linha["Tipo"].toString().toBigDecimal().toInt(),
            docEntry = linha["DocEntry"].toString().toBigDecimal().toInt(),
            docNum = linha["DocNum"]?.toString()?.toBigDecimalOrNull()?.toInt(),
            data = linha["Data"]?.toString(),
            cliente = linha["Cliente"]?.toString(),
            clienteNome = linha["ClienteNome"]?.toString(),
            item = linha["Item"].toString(),
            lote = linha["Lote"].toString(),
            quantidade = BigDecimal(linha["Quantidade"].toString()),
        )
    }
}

/**
 * O que pedir ao banco (ver rastreio-lote-movimentos.sql): as OPs que geraram algum lote de `acima`, as
 * que geraram o lote digitado (`loteGerado`, sem item) ou as que o consumiram (`loteConsumido`).
 */
data class BuscaLotes(
    val loteGerado: String = "",
    val loteConsumido: String = "",
    val acima: Set<LoteChave> = setOf(),
    //as OPs que consumiram algum desses lotes (destino)
    val abaixo: Set<LoteChave> = setOf(),
)

/** Um nivel do rastreio, separado do banco. `buscar(busca, limite)` devolve todos os movimentos das OPs da busca. */
class RastreioLote(
    private val buscar: (busca: BuscaLotes, limite: Int) -> List<MovimentoLote>,
    private val vendas: (lotes: Set<LoteChave>) -> List<VendaLote> = { listOf() },
    private val maxOrdens: Int = MAX_ORDENS,
) {

    fun nivel(loteInformado: String, itemInformado: String? = null): MapaRelacoesResponse {
        val lote = loteInformado.trim()
        if (lote.isEmpty())
            throw IllegalArgumentException("Informe o número do lote.")
        val item = itemInformado?.trim()?.takeIf { it.isNotEmpty() }
        fun doLote(m: MovimentoLote) = m.lote == lote && (item == null || m.item == item)

        //quem gerou o lote: e o que interessa. No clique o item vem junto; na busca digitada, nao.
        val movimentos = if (item != null)
            buscar(BuscaLotes(acima = setOf(LoteChave(item, lote))), maxOrdens)
        else
            buscar(BuscaLotes(loteGerado = lote), maxOrdens)
        val geracoes = movimentos.filter { it.gerou && doLote(it) }

        //ninguem gerou: lote comprado. Uma OP que o consumiu basta para dizer de que item ele e (e o nome).
        val consumos = if (geracoes.isEmpty()) buscar(BuscaLotes(loteConsumido = lote), 1).filter { it.consumiu && doLote(it) }
        else listOf()
        val buscados = (geracoes + consumos).map { it.chave }.toSet()
        if (buscados.isEmpty())
            throw IllegalArgumentException("O lote $lote não foi gerado nem consumido por nenhuma ordem de produção.")

        val ordens = geracoes.map { it.ordem }.distinct().sortedDescending()
        val porOrdem = movimentos.filter { it.ordem in ordens }.groupBy { it.ordem }
        val insumos = porOrdem.values.flatten().filter { it.consumiu }

        //olha um nivel a frente so para saber quais insumos tem origem para abrir no clique
        val comOrigem = if (insumos.isEmpty()) setOf()
        else buscar(BuscaLotes(acima = insumos.map { it.chave }.toSet()), maxOrdens).filter { it.gerou }.map { it.chave }.toSet()

        val nomes = (movimentos + consumos).associate { it.chave to it.itemNome }
        val nodes = linkedMapOf<String, MapaNode>()
        //todo lote abre o destino (quem consumiu, quem comprou): no insumo, sao as outras OPs que tambem
        //o usaram e, por elas, onde mais ele pode ter sido vendido
        buscados.forEach { chave ->
            nodes[idLote(chave)] = noLote(chave, nomes[chave], if (chave in geracoes.map { it.chave }) null else SEM_ORDEM_PRODUCAO,
                destino = true)
        }
        porOrdem.forEach { (ordem, linhas) -> nodes[idOrdem(ordem)] = noOrdem(ordem, linhas) }
        insumos.map { it.chave }.distinct().forEach { chave ->
            nodes.putIfAbsent(idLote(chave), noLote(chave, nomes[chave], if (chave in comOrigem) ORIGEM_DISPONIVEL else SEM_ORDEM_PRODUCAO,
                destino = true))
        }

        //lote consumido -> OP -> lote gerado, com o numero do lote escrito na aresta
        val edges = mutableListOf<MapaEdge>()
        porOrdem.forEach { (ordem, linhas) ->
            linhas.filter { it.consumiu }.map { it.chave }.distinct().forEach { chave ->
                edges += MapaEdge("${idLote(chave)}>${idOrdem(ordem)}", idLote(chave), idOrdem(ordem), TipoAresta.LOTE, chave.lote)
            }
            linhas.filter { it.gerou && it.chave in buscados }.map { it.chave }.distinct().forEach { chave ->
                edges += MapaEdge("${idOrdem(ordem)}>${idLote(chave)}", idOrdem(ordem), idLote(chave), TipoAresta.LOTE, chave.lote)
            }
        }

        val aviso = if (ordens.isEmpty())
            "O lote $lote não foi gerado por ordem de produção (comprado ou lançado por entrada manual): não há origem para mostrar."
        else null
        return MapaRelacoesResponse(idLote(buscados.first()), nodes.values.toList(), edges, aviso)
    }

    /**
     * Para onde o lote foi: as OPs que o consumiram, os lotes que elas geraram (que abrem o destino de
     * novo) e os documentos de venda com o cliente. Um lote por vez, pelo item: no clique da tela.
     */
    fun destino(loteInformado: String, itemInformado: String): MapaRelacoesResponse {
        val chave = LoteChave(itemInformado.trim(), loteInformado.trim())
        if (chave.item.isEmpty() || chave.lote.isEmpty())
            throw IllegalArgumentException("Informe o lote e o item.")

        val movimentos = buscar(BuscaLotes(abaixo = setOf(chave)), maxOrdens)
        val ordens = movimentos.filter { it.consumiu && it.chave == chave }.map { it.ordem }.distinct().sortedDescending()
        val porOrdem = movimentos.filter { it.ordem in ordens }.groupBy { it.ordem }
        val documentos = vendas(setOf(chave)).filter { it.chave == chave }

        val nomes = movimentos.associate { it.chave to it.itemNome }
        val nodes = linkedMapOf<String, MapaNode>()
        nodes[idLote(chave)] = noLote(chave, nomes[chave], null, destino = true)
        val edges = mutableListOf<MapaEdge>()
        porOrdem.forEach { (ordem, linhas) ->
            nodes[idOrdem(ordem)] = noOrdem(ordem, linhas)
            edges += MapaEdge("${idLote(chave)}>${idOrdem(ordem)}", idLote(chave), idOrdem(ordem), TipoAresta.LOTE, chave.lote)
            linhas.filter { it.gerou }.map { it.chave }.distinct().forEach { gerado ->
                nodes.putIfAbsent(idLote(gerado), noLote(gerado, nomes[gerado], null, destino = true))
                edges += MapaEdge("${idOrdem(ordem)}>${idLote(gerado)}", idOrdem(ordem), idLote(gerado), TipoAresta.LOTE, gerado.lote)
            }
        }
        documentos.forEach { venda ->
            val no = noVenda(venda)
            nodes[no.id] = no
            edges += MapaEdge("${idLote(chave)}>${no.id}", idLote(chave), no.id, TipoAresta.LOTE, chave.lote)
        }

        val aviso = if (porOrdem.isEmpty() && documentos.isEmpty())
            "O lote ${chave.lote} ainda não foi consumido por ordem de produção nem vendido."
        else null
        return MapaRelacoesResponse(idLote(chave), nodes.values.toList(), edges, aviso)
    }

    private fun noVenda(venda: VendaLote): MapaNode {
        val tipo = when (venda.tipo) {
            13 -> MapaTipoDocumento.NOTA_FISCAL
            15 -> MapaTipoDocumento.ENTREGA
            else -> MapaTipoDocumento.DEVOLUCAO
        }
        val sentido = if (venda.quantidade.signum() < 0) "Saiu" else "Voltou"
        return MapaNode(
            id = "${tipo.name}:${venda.tipo}:${venda.docEntry}",
            tipo = tipo,
            //so a nota tem tela aberta pelo DocEntry; o resto fica sem o botao de abrir
            docEntry = if (venda.tipo == 13) venda.docEntry else null,
            docNum = venda.docNum?.toString(),
            cardCode = venda.cliente,
            label = "${tipo.label} ${venda.docNum ?: venda.docEntry}",
            valor = null,
            data = venda.data,
            status = null,
            detalhes = listOfNotNull(
                listOfNotNull(venda.cliente, venda.clienteNome).joinToString(" - ").ifEmpty { null },
                "$sentido: ${quantidade(venda.quantidade)}",
            ),
        )
    }

    private fun noOrdem(ordem: Int, linhas: List<MovimentoLote>): MapaNode {
        val primeira = linhas.firstOrNull()
        val gerados = linhas.filter { it.gerou }
        val detalhes = listOfNotNull(
            primeira?.let { listOfNotNull(it.ordemItem, it.ordemItemNome).joinToString(" - ") },
            if (primeira?.ordemTipo == "D") "Desmontagem" else null,
            gerados.takeIf { it.isNotEmpty() }?.let { lista ->
                "Gerou: " + lista.joinToString(", ") { "${it.lote} (${quantidade(it.quantidade)})" }
            },
        )
        return MapaNode(
            id = idOrdem(ordem),
            tipo = MapaTipoDocumento.ORDEM_PRODUCAO,
            docEntry = ordem,
            docNum = primeira?.ordemNumero?.toString(),
            cardCode = null,
            label = "Ordem de Produção ${primeira?.ordemNumero ?: ordem}",
            valor = null,
            data = primeira?.ordemData,
            status = STATUS[primeira?.ordemStatus] ?: primeira?.ordemStatus,
            detalhes = detalhes,
        )
    }

    private fun noLote(chave: LoteChave, nome: String?, situacao: String?, destino: Boolean = false) = MapaNode(
        id = idLote(chave),
        tipo = MapaTipoDocumento.LOTE,
        docEntry = null,
        docNum = chave.lote,
        cardCode = null,
        label = "Lote ${chave.lote}",
        valor = null,
        data = null,
        status = null,
        situacao = situacao,
        detalhes = listOf(listOfNotNull(chave.item, nome).joinToString(" - ")),
        item = chave.item,
        destino = destino,
    )

    companion object {
        const val MAX_ORDENS = 150

        //situacao do no de lote: a tela abre o nivel seguinte no clique / lote comprado, nada a abrir
        const val ORIGEM_DISPONIVEL = "ORIGEM_DISPONIVEL"
        const val SEM_ORDEM_PRODUCAO = "SEM_ORDEM_PRODUCAO"

        //OWOR.Status
        private val STATUS = mapOf("P" to "Planejada", "R" to "Liberada", "L" to "Fechada", "C" to "Cancelada")

        fun idOrdem(ordem: Int) = "ORDEM_PRODUCAO:$ordem"
        fun idLote(chave: LoteChave) = "LOTE:${chave.item}:${chave.lote}"

        private fun quantidade(valor: BigDecimal) = valor.abs().stripTrailingZeros().toPlainString()
    }
}
