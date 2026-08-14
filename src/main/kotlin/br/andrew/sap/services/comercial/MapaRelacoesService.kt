package br.andrew.sap.services.comercial

import JournalEntry
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.comercial.MapaEdge
import br.andrew.sap.model.comercial.MapaNode
import br.andrew.sap.model.comercial.MapaRelacoesResponse
import br.andrew.sap.model.comercial.MapaTipoDocumento
import br.andrew.sap.model.comercial.SituacaoNode
import br.andrew.sap.model.comercial.TipoAresta
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.documents.QuotationsService
import br.andrew.sap.services.financeiro.InternalReconciliationsService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.model.transaction.TransactionCodeTypes
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class MapaRelacoesService(
    val contratoService: ContratoVendaFuturaService,
    val orderService: OrdersService,
    val quotationsService: QuotationsService,
    val invoiceService: InvoiceService,
    val creditNotesService: CreditNotesService,
    val downPaymentService: DownPaymentService,
    val incomingPaymentService: IncomingPaymentService,
    val businessPartnersService: BusinessPartnersService,
    val internalReconciliationsService: InternalReconciliationsService,
    val journalEntriesService: JournalEntriesService,
    val sqlQueriesService: SqlQueriesService
) {

    //so esses TransactionCode contam como "lancamento contabil do contrato" no mapa:
    //VFET = reclassificacao da entrega ainda pendente de baixa; VFEC = a mesma
    //reclassificacao ja baixada (retagueada quando o adiantamento e apropriado contra
    //ela); VFDV = reclassificacao de devolucao; CanC = cancelado; AROU = "Outros"
    //(ajuste de spread da BaixaSpreadVendaFuturaService e baixa de adiantamento com
    //devolucao da EstornoReclassificacaoVendaFuturaService).
    private val transactionCodesRelevantes = setOf(
        TransactionCodeTypes.VFET.toString(),
        TransactionCodeTypes.VFDV.toString(),
        TransactionCodeTypes.VFEC.toString(),
        TransactionCodeTypes.CanC.toString(),
        TransactionCodeTypes.AROU.toString(),
    )

    private val servicosPorTipoVendaFutura: List<Pair<MapaTipoDocumento, EntitiesService<Document>>> = listOf(
        MapaTipoDocumento.PEDIDO to orderService,
        MapaTipoDocumento.NOTA_FISCAL to invoiceService,
        MapaTipoDocumento.ADIANTAMENTO to downPaymentService,
    )

    fun mapa(tipo: MapaTipoDocumento, docEntry: Int): MapaRelacoesResponse {
        val raiz = encontrarRaiz(tipo, docEntry)
        return expandir(raiz)
    }

    private sealed class Raiz {
        class DeContrato(val contrato: Contrato) : Raiz()
        class DeDocumento(val tipo: MapaTipoDocumento, val doc: Document) : Raiz()
    }

    /**
     * Sobe a cadeia de documentos ate achar a raiz de verdade: o Contrato de
     * Venda Futura (quando existir em qualquer ponto da cadeia), ou o
     * documento mais a montante (sem U_venda_futura, sem Contrato apontando
     * pra ele, sem linha com BaseType/BaseEntry).
     */
    private fun encontrarRaiz(tipoInicial: MapaTipoDocumento, docEntryInicial: Int): Raiz {
        if (tipoInicial == MapaTipoDocumento.CONTRATO)
            return Raiz.DeContrato(contratoService.getById(docEntryInicial).tryGetValue())

        var tipoAtual = tipoInicial
        var doc = fetchDocumento(tipoAtual, docEntryInicial)
            ?: throw Exception("Documento nao encontrado: $tipoInicial $docEntryInicial")
        val visitados = mutableSetOf<Pair<MapaTipoDocumento, Int>>()

        while (true) {
            val chave = tipoAtual to (doc.docEntry ?: -1)
            if (!visitados.add(chave))
                return Raiz.DeDocumento(tipoAtual, doc)

            val vendaFutura = doc.U_venda_futura
            if (vendaFutura != null && vendaFutura != 0)
                return Raiz.DeContrato(contratoService.getById(vendaFutura).tryGetValue())

            if (tipoAtual == MapaTipoDocumento.PEDIDO) {
                val contrato = contratoService
                    .get(Filter("U_orderDocEntry", doc.docEntry ?: -1, Condicao.EQUAL))
                    .tryGetValues<Contrato>()
                    .firstOrNull()
                if (contrato != null)
                    return Raiz.DeContrato(contrato)
            }

            val base = doc.DocumentLines.firstOrNull { it.BaseType != null && it.BaseEntry != null }
                ?: return Raiz.DeDocumento(tipoAtual, doc)
            val proximoTipo = tipoPorObjType(base.BaseType!!) ?: return Raiz.DeDocumento(tipoAtual, doc)
            val proximoDoc = fetchDocumento(proximoTipo, base.BaseEntry!!) ?: return Raiz.DeDocumento(tipoAtual, doc)

            tipoAtual = proximoTipo
            doc = proximoDoc
        }
    }

    /**
     * BFS a partir da raiz - cada nó só é expandido (filhosDe...) uma vez,
     * o que evita loop infinito mesmo se conciliações internas formarem um
     * ciclo entre documentos.
     */
    private fun expandir(raiz: Raiz): MapaRelacoesResponse {
        val nodes = linkedMapOf<String, MapaNode>()
        val edges = linkedMapOf<String, MapaEdge>()
        val expandidos = mutableSetOf<String>()
        val fila = ArrayDeque<Triple<String, MapaTipoDocumento, Int>>()

        val cardCode = when (raiz) {
            is Raiz.DeContrato -> raiz.contrato.U_cardCode
            is Raiz.DeDocumento -> raiz.doc.CardCode
        }
        val cliente = businessPartnersService.getById("'$cardCode'").tryGetValue<BusinessPartner>()
        val clienteKey = chave(MapaTipoDocumento.CLIENTE, cliente.cardCode ?: cardCode)
        nodes[clienteKey] = nodeCliente(cliente, cardCode)

        val raizKey = when (raiz) {
            is Raiz.DeContrato -> {
                val docEntry = raiz.contrato.DocEntry ?: -1
                val key = chave(MapaTipoDocumento.CONTRATO, docEntry)
                nodes[key] = nodeContrato(raiz.contrato)
                fila.add(Triple(key, MapaTipoDocumento.CONTRATO, docEntry))
                key
            }
            is Raiz.DeDocumento -> {
                val docEntry = raiz.doc.docEntry ?: -1
                val key = chave(raiz.tipo, docEntry)
                nodes[key] = nodeDocumento(raiz.tipo, raiz.doc)
                fila.add(Triple(key, raiz.tipo, docEntry))
                key
            }
        }
        addEdge(edges, clienteKey, raizKey, TipoAresta.ORIGEM)

        while (fila.isNotEmpty()) {
            val (paiKey, tipoPai, docEntryPai) = fila.removeFirst()
            if (!expandidos.add(paiKey))
                continue

            val filhos = if (tipoPai == MapaTipoDocumento.CONTRATO)
                filhosDeContrato(docEntryPai, nodes)
            else
                filhosDeDocumento(tipoPai, docEntryPai, nodes[paiKey]?.docNum)

            for ((tipoFilho, docEntryFilho, tipoAresta) in filhos) {
                val filhoKey = chave(tipoFilho, docEntryFilho)
                if (!nodes.containsKey(filhoKey)) {
                    val node = construirNode(tipoFilho, docEntryFilho) ?: continue
                    nodes[filhoKey] = node
                }
                addEdge(edges, paiKey, filhoKey, tipoAresta)
                //RECEBIMENTO, LANCAMENTO_CONTABIL e OUTRO sao folhas - nao geram mais documentos nesse dominio
                if (filhoKey !in expandidos
                    && tipoFilho != MapaTipoDocumento.RECEBIMENTO
                    && tipoFilho != MapaTipoDocumento.LANCAMENTO_CONTABIL
                    && tipoFilho != MapaTipoDocumento.OUTRO)
                    fila.add(Triple(filhoKey, tipoFilho, docEntryFilho))
            }
        }

        //root = o documento que originou tudo (Contrato ou o documento mais a montante),
        //NAO o cliente - o cliente e so mais um no no grafo (o "avo" de tudo)
        val semConciliacaoRedundante = removerConciliacaoRedundante(edges, nodes)
        val edgesFinais = removerGeradoParaContratoRedundante(semConciliacaoRedundante)
        return MapaRelacoesResponse(raizKey, nodes.values.toList(), edgesFinais)
    }

    /**
     * Uma Devolucao pode aparecer tanto ligada direto ao Contrato (U_venda_futura,
     * GERADO_PARA_CONTRATO) quanto via conciliacao (ITR1) com o documento que ela
     * estorna (ex.: a Nota Fiscal). Quando as duas existem, a aresta de conciliacao
     * fica redundante - a Devolucao ja esta ancorada no grafo pelo vinculo direto
     * com o contrato, entao a de conciliacao so polui o desenho.
     */
    private fun removerConciliacaoRedundante(edges: Map<String, MapaEdge>, nodes: Map<String, MapaNode>): List<MapaEdge> {
        val devolucoesLigadasAoContrato = edges.values
            .filter { it.tipo == TipoAresta.GERADO_PARA_CONTRATO && nodes[it.to]?.tipo == MapaTipoDocumento.DEVOLUCAO }
            .map { it.to }
            .toSet()

        return edges.values.filterNot { e ->
            e.tipo == TipoAresta.CONCILIACAO && (e.from in devolucoesLigadasAoContrato || e.to in devolucoesLigadasAoContrato)
        }
    }

    //arestas que ja "ancoram" um documento no grafo por outro caminho estrutural,
    //tornando o vinculo direto com o contrato redundante - copia de documento
    //(ex.: Nota Fiscal copiada de Pedido, Pedido copiado de Cotacao) e apropriacao
    //de adiantamento (ex.: Nota Fiscal que sacou de um Adiantamento ja ligado ao
    //contrato)
    private val arestasQueAncoramNoGrafo = setOf(TipoAresta.COPIA_DOCUMENTO, TipoAresta.APROPRIACAO)

    /**
     * Um documento que ja chega ao grafo por uma dessas arestas (ver
     * arestasQueAncoramNoGrafo) nao precisa TAMBEM do vinculo direto com o Contrato
     * (U_venda_futura, GERADO_PARA_CONTRATO) - ele ja esta ancorado no grafo por
     * quem o gerou/apropriou, que por sua vez ja chega ate o Contrato. So mantem o
     * vinculo direto quando NAO existe nenhum desses caminhos (ex.: um Adiantamento
     * em si, que nunca e copiado nem apropriado de outro documento).
     */
    private fun removerGeradoParaContratoRedundante(edges: List<MapaEdge>): List<MapaEdge> {
        val documentosJaAncorados = edges
            .filter { it.tipo in arestasQueAncoramNoGrafo }
            .map { it.to }
            .toSet()

        return edges.filterNot { e ->
            e.tipo == TipoAresta.GERADO_PARA_CONTRATO && e.to in documentosJaAncorados
        }
    }

    private fun filhosDeContrato(idContrato: Int, nodes: MutableMap<String, MapaNode>): List<Triple<MapaTipoDocumento, Int, TipoAresta>> {
        val contrato = contratoService.getById(idContrato).tryGetValue<Contrato>()
        val filhos = mutableListOf<Triple<MapaTipoDocumento, Int, TipoAresta>>()

        //o pedido original as vezes ja foi fechado/alterado no SAP e o fetch pode falhar -
        //tenta buscar os dados completos, mas sempre garante o no usando o que o proprio
        //Contrato ja guarda em cache (U_orderDocEntry/OrderDocNum), pra nunca sumir com a relacao
        val pedidoOrigemKey = chave(MapaTipoDocumento.PEDIDO, contrato.U_orderDocEntry)
        if (!nodes.containsKey(pedidoOrigemKey))
            nodes[pedidoOrigemKey] = fetchDocumento(MapaTipoDocumento.PEDIDO, contrato.U_orderDocEntry)
                ?.let { nodeDocumento(MapaTipoDocumento.PEDIDO, it) }
                ?: nodePedidoOrigemDeContrato(contrato)
        filhos += Triple(MapaTipoDocumento.PEDIDO, contrato.U_orderDocEntry, TipoAresta.PEDIDO_ORIGEM)

        val filtro = Filter("U_venda_futura", idContrato, Condicao.EQUAL)
        for ((tipo, service) in servicosPorTipoVendaFutura)
            service.getAll(Document::class.java, filtro)
                .forEach { doc -> doc.docEntry?.let { filhos += Triple(tipo, it, TipoAresta.GERADO_PARA_CONTRATO) } }
        //cotacoes (ex.: pedido-retirada de entrega parcial) e devolucoes tambem podem levar U_venda_futura
        quotationsService.getAll(Document::class.java, filtro)
            .forEach { doc -> doc.docEntry?.let { filhos += Triple(MapaTipoDocumento.COTACAO, it, TipoAresta.GERADO_PARA_CONTRATO) } }
        creditNotesService.getAll(Document::class.java, filtro)
            .forEach { doc -> doc.docEntry?.let { filhos += Triple(MapaTipoDocumento.DEVOLUCAO, it, TipoAresta.GERADO_PARA_CONTRATO) } }

        return filhos.distinct()
    }

    private fun filhosDeDocumento(tipo: MapaTipoDocumento, docEntry: Int, docNum: String?): List<Triple<MapaTipoDocumento, Int, TipoAresta>> {
        val objType = objTypePorTipo(tipo) ?: return listOf()
        val filhos = mutableListOf<Triple<MapaTipoDocumento, Int, TipoAresta>>()

        sqlQueriesService.execute(
            "base-document-filhos.sql",
            listOf(Parameter("baseType", objType), Parameter("baseEntry", docEntry))
        )?.tryGetValues<BaseDocumentFilho>()?.forEach { f ->
            tipoPorObjType(f.ObjType)?.let { filhos += Triple(it, f.DocEntry, TipoAresta.COPIA_DOCUMENTO) }
        }

        internalReconciliationsService.contrapartidasDocumentos(docEntry, objType).forEach { c ->
            val tipoContrapartida = tipoPorObjType(c.SrcObjTyp) ?: MapaTipoDocumento.OUTRO
            //a view SQL (reconciliacao-interna-contrapartidas-docs.sql) retorna qualquer outra
            //linha da mesma reconciliacao (ITR1/OITR), sem distinguir lado debito/credito - entao
            //quando varios adiantamentos sao apropriados juntos pela mesma nota (ex.: 3 adiantamentos
            //num unico INV9), cada um aparece como "contrapartida" dos outros, mesmo estando do
            //mesmo lado (credito). Dois adiantamentos nunca sao conciliados um com o outro de
            //verdade - eles so compartilham o ponto em comum (a nota de apropriacao/lancamento),
            //que ja aparece como aresta separada. Sem esse filtro o grafo desenha uma aresta a mais
            //entre cada par de adiantamentos, sem necessidade.
            if (tipo == MapaTipoDocumento.ADIANTAMENTO && tipoContrapartida == MapaTipoDocumento.ADIANTAMENTO)
                return@forEach
            //lancamento contabil so entra se for um dos TransactionCode relevantes
            //(ver transactionCodesRelevantes) - evita poluir o grafo com lancamentos
            //automaticos do SAP sem relacao com a regra de negocio de venda futura
            if (tipoContrapartida != MapaTipoDocumento.LANCAMENTO_CONTABIL || transactionCodeRelevante(c.SrcObjAbs))
                filhos += Triple(tipoContrapartida, c.SrcObjAbs, TipoAresta.CONCILIACAO)
        }

        //lancamentos de reclassificacao/baixa que citam o DocNum DESSE documento em
        //Reference - NAO o do contrato. Confirmado em todos os pontos do codigo que
        //criam esses lancamentos: ReclassificacaoEntregaVendaFuturaSchedule seta
        //Reference = invoice.docNum (a propria Nota Fiscal, TransactionCode VFET,
        //depois re-tagueado pra VFEC por ConciliacaoVendaFuturaSchedule) e
        //EstornoReclassificacaoVendaFuturaService seta Reference = devolucao.docNum
        //(TransactionCode VFDV). Complementa a via de conciliacao ITR1/OITR acima,
        //que cobre quando existe InternalReconciliations mas nao cobre sozinha todo
        //lancamento manual (ex.: antes de qualquer conciliacao ser feita).
        if ((tipo == MapaTipoDocumento.NOTA_FISCAL || tipo == MapaTipoDocumento.DEVOLUCAO) && docNum != null) {
            val filtroLancamentos = Filter(
                Predicate("Reference", docNum, Condicao.EQUAL),
                Predicate("TransactionCode", transactionCodesRelevantes.toList(), Condicao.IN),
            )
            journalEntriesService.getAll(JournalEntry::class.java, filtroLancamentos)
                .forEach { j -> j.JdtNum?.let { filhos += Triple(MapaTipoDocumento.LANCAMENTO_CONTABIL, it, TipoAresta.RECLASSIFICACAO) } }
        }

        if (tipo == MapaTipoDocumento.ADIANTAMENTO)
            downPaymentService.invoicesApropriados(docEntry)
                .forEach { filhos += Triple(MapaTipoDocumento.NOTA_FISCAL, it, TipoAresta.APROPRIACAO) }

        return filhos.distinct()
    }

    /**
     * Sempre desserializa como Document generico (nunca como a subclasse
     * especifica, ex.: OrderSales) - OrderSales redeclara seu proprio
     * "DocEntry" (String, pra outro fim), que sombreia o Document.docEntry
     * (Int) herdado pra efeito de Jackson, ja que os dois mapeiam pro mesmo
     * JSON "DocEntry". Como aqui so usamos campos da base (docNum, CardCode,
     * DocTotal, docDate, DocumentStatus, docEntry), Document generico basta
     * e evita esse conflito.
     */
    private fun fetchDocumento(tipo: MapaTipoDocumento, docEntry: Int): Document? {
        return try {
            when (tipo) {
                MapaTipoDocumento.COTACAO -> quotationsService.getById(docEntry).tryGetValue<Document>()
                MapaTipoDocumento.PEDIDO -> orderService.getById(docEntry).tryGetValue<Document>()
                MapaTipoDocumento.NOTA_FISCAL -> invoiceService.getById(docEntry).tryGetValue<Document>()
                MapaTipoDocumento.ADIANTAMENTO -> downPaymentService.getById(docEntry).tryGetValue<Document>()
                MapaTipoDocumento.DEVOLUCAO -> creditNotesService.getById(docEntry).tryGetValue<Document>()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun construirNode(tipo: MapaTipoDocumento, docEntry: Int): MapaNode? {
        return when (tipo) {
            MapaTipoDocumento.RECEBIMENTO -> try {
                nodeRecebimento(incomingPaymentService.getById(docEntry).tryGetValue())
            } catch (e: Exception) {
                null
            }
            MapaTipoDocumento.LANCAMENTO_CONTABIL -> try {
                journalEntriesService.getByDocEntry(docEntry)?.let { nodeLancamentoContabil(it) }
            } catch (e: Exception) {
                null
            }
            MapaTipoDocumento.OUTRO -> MapaNode(
                id = chave(MapaTipoDocumento.OUTRO, docEntry), tipo = MapaTipoDocumento.OUTRO, docEntry = docEntry,
                docNum = null, cardCode = null, label = "Documento #$docEntry", valor = null, data = null, status = null
            )
            else -> fetchDocumento(tipo, docEntry)?.let { nodeDocumento(tipo, it) }
        }
    }

    private fun chave(tipo: MapaTipoDocumento, docEntry: Any?): String = "$tipo:$docEntry"

    private fun addEdge(edges: MutableMap<String, MapaEdge>, from: String, to: String, tipo: TipoAresta) {
        val id = "$from->$to:$tipo"
        edges.putIfAbsent(id, MapaEdge(id, from, to, tipo))
    }

    private fun nodeCliente(bp: BusinessPartner, cardCodeFallback: String): MapaNode {
        val cardCode = bp.cardCode ?: cardCodeFallback
        return MapaNode(
            id = chave(MapaTipoDocumento.CLIENTE, cardCode), tipo = MapaTipoDocumento.CLIENTE, docEntry = null,
            docNum = null, cardCode = cardCode, label = bp.cardName ?: cardCode, valor = null, data = null, status = null
        )
    }

    private fun nodeContrato(c: Contrato): MapaNode {
        val docEntry = c.DocEntry ?: -1
        return MapaNode(
            id = chave(MapaTipoDocumento.CONTRATO, docEntry), tipo = MapaTipoDocumento.CONTRATO, docEntry = docEntry,
            docNum = c.DocNum?.toString(), cardCode = c.U_cardCode, label = "Contrato ${c.DocNum ?: docEntry}",
            valor = c.total(), data = c.U_dataCriacao, status = c.U_status.toString()
        )
    }

    //fallback quando o pedido original do contrato nao consegue ser buscado no SAP -
    //usa so o que o Contrato ja tem em cache, sem data/valor/status (nao temos essa info aqui)
    private fun nodePedidoOrigemDeContrato(c: Contrato): MapaNode {
        val docEntry = c.U_orderDocEntry
        return MapaNode(
            id = chave(MapaTipoDocumento.PEDIDO, docEntry), tipo = MapaTipoDocumento.PEDIDO, docEntry = docEntry,
            docNum = c.OrderDocNum, cardCode = c.U_cardCode,
            label = "${MapaTipoDocumento.PEDIDO.label} ${c.OrderDocNum ?: docEntry}",
            valor = null, data = null, status = null
        )
    }

    private fun nodeDocumento(tipo: MapaTipoDocumento, doc: Document): MapaNode {
        return MapaNode(
            id = chave(tipo, doc.docEntry), tipo = tipo, docEntry = doc.docEntry, docNum = doc.docNum,
            cardCode = doc.CardCode, label = "${tipo.label} ${doc.docNum ?: doc.docEntry ?: ""}".trim(),
            valor = doc.DocTotal?.toBigDecimalOrNull(), data = doc.docDate,
            status = doc.DocumentStatus?.typeName ?: doc.Cancelled?.toString(),
            situacao = if (tipo == MapaTipoDocumento.ADIANTAMENTO) situacaoAdiantamento(doc) else null
        )
    }

    /**
     * Adiantamento que ainda tem saldo pra ser apropriado por alguma nota fiscal. O valor
     * ja sacado vem da soma de INV9.DrawnSum (DownPaymentService.valorApropriado) - vale
     * tanto pro adiantamento nunca utilizado quanto pro utilizado so em parte. Falha na
     * consulta nao pode derrubar o mapa: sem o valor, o card fica simplesmente sem etiqueta.
     */
    private fun situacaoAdiantamento(doc: Document): String? {
        val docEntry = doc.docEntry ?: return null
        val total = doc.DocTotal?.toBigDecimalOrNull() ?: return null
        val apropriado = runCatching { downPaymentService.valorApropriado(docEntry) }.getOrNull() ?: return null
        return if (total > apropriado) SituacaoNode.PENDENTE_UTILIZACAO else null
    }

    private fun transactionCodeRelevante(jdtNum: Int): Boolean {
        return try {
            journalEntriesService.getByDocEntry(jdtNum)?.TransactionCode in transactionCodesRelevantes
        } catch (e: Exception) {
            false
        }
    }

    //valor = soma dos debitos das linhas (== soma dos creditos, lancamento sempre balanceado)
    private fun nodeLancamentoContabil(j: JournalEntry): MapaNode {
        return MapaNode(
            id = chave(MapaTipoDocumento.LANCAMENTO_CONTABIL, j.JdtNum), tipo = MapaTipoDocumento.LANCAMENTO_CONTABIL,
            docEntry = j.JdtNum, docNum = j.JdtNum?.toString(), cardCode = null,
            label = "Lançamento Contábil ${j.JdtNum ?: ""}".trim(),
            valor = j.journalEntryLines.sumOf { it.debit }.toBigDecimal(),
            //TransactionCode vai em situacao (vira etiqueta colorida) - lancamento contabil
            //nao tem DocumentStatus, entao status fica vazio
            data = j.taxDate, status = null, situacao = j.TransactionCode
        )
    }

    private fun nodeRecebimento(p: Payment): MapaNode {
        return MapaNode(
            id = chave(MapaTipoDocumento.RECEBIMENTO, p.docEntry), tipo = MapaTipoDocumento.RECEBIMENTO, docEntry = p.docEntry,
            docNum = p.docNum?.toString(), cardCode = p.cardCode, label = "Recebimento ${p.docNum ?: p.docEntry ?: ""}".trim(),
            //DocTotal e o total do recebimento em qualquer forma de pagamento - cashSum
            //(so especie) fica de fallback, mas na pratica vem 0 em boleto/transferencia/PIX
            valor = (p.DocTotal ?: p.cashSum)?.toBigDecimal(), data = p.docDate, status = null
        )
    }

    private fun objTypePorTipo(tipo: MapaTipoDocumento): Int? = when (tipo) {
        MapaTipoDocumento.COTACAO -> DocumentTypes.oQuotations.value
        MapaTipoDocumento.PEDIDO -> DocumentTypes.oOrders.value
        MapaTipoDocumento.NOTA_FISCAL -> DocumentTypes.oInvoices.value
        MapaTipoDocumento.ADIANTAMENTO -> DocumentTypes.oDownPayments.value
        MapaTipoDocumento.DEVOLUCAO -> DocumentTypes.oCreditNotes.value
        MapaTipoDocumento.RECEBIMENTO -> DocumentTypes.oIncomingPayments.value
        MapaTipoDocumento.LANCAMENTO_CONTABIL -> DocumentTypes.oJournalEntries.value
        else -> null
    }

    private fun tipoPorObjType(objType: Int): MapaTipoDocumento? = when (objType) {
        DocumentTypes.oQuotations.value -> MapaTipoDocumento.COTACAO
        DocumentTypes.oOrders.value -> MapaTipoDocumento.PEDIDO
        DocumentTypes.oInvoices.value -> MapaTipoDocumento.NOTA_FISCAL
        DocumentTypes.oDownPayments.value -> MapaTipoDocumento.ADIANTAMENTO
        DocumentTypes.oCreditNotes.value -> MapaTipoDocumento.DEVOLUCAO
        DocumentTypes.oIncomingPayments.value -> MapaTipoDocumento.RECEBIMENTO
        DocumentTypes.oJournalEntries.value -> MapaTipoDocumento.LANCAMENTO_CONTABIL
        else -> null
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BaseDocumentFilho(val ObjType: Int, val DocEntry: Int)
