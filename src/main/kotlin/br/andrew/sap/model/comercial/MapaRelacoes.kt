package br.andrew.sap.model.comercial

import java.math.BigDecimal

enum class MapaTipoDocumento(val label: String) {
    CLIENTE("Cliente"),
    CONTRATO("Contrato de Venda Futura"),
    COTACAO("Cotação"),
    PEDIDO("Pedido de Venda"),
    NOTA_FISCAL("Nota Fiscal"),
    ADIANTAMENTO("Adiantamento"),
    DEVOLUCAO("Devolução"),
    RECEBIMENTO("Recebimento"),
    LANCAMENTO_CONTABIL("Lançamento Contábil"),
    //rastreio de lote (RastreioLoteService): as OPs e o lote buscado
    ORDEM_PRODUCAO("Ordem de Produção"),
    LOTE("Lote"),
    //entrega de venda (ODLN) - destino do rastreio de lote
    ENTREGA("Entrega"),
    OUTRO("Outro")
}

enum class TipoAresta {
    //cliente -> raiz da arvore (contrato ou o proprio documento, quando nao ha contrato)
    ORIGEM,
    //contrato -> pedido que o originou (Contrato.U_orderDocEntry)
    PEDIDO_ORIGEM,
    //contrato -> documento gerado pra ele (U_venda_futura == contrato.DocEntry)
    GERADO_PARA_CONTRATO,
    //documento -> documento copiado dele via BaseType/BaseEntry (ex.: pedido -> nota)
    COPIA_DOCUMENTO,
    //documento -> contrapartida de reconciliacao interna (ITR1/OITR)
    CONCILIACAO,
    //adiantamento -> nota fiscal que sacou dele (INV9)
    APROPRIACAO,
    //nota fiscal ou devolucao -> lancamento contabil manual cujo Reference cita o
    //DocNum desse documento (nunca o do contrato) - convencao usada por
    //ReclassificacaoEntregaVendaFuturaSchedule (NF, VFET/VFEC) e
    //EstornoReclassificacaoVendaFuturaService (devolucao, VFDV)
    RECLASSIFICACAO,
    //rastreio de lote: lote consumido -> OP -> lote gerado; MapaEdge.rotulo leva o numero do lote
    LOTE
}

//valores de MapaNode.situacao que nao vem direto de um campo do SAP
object SituacaoNode {
    //adiantamento que ainda tem saldo nao apropriado por nenhuma nota fiscal
    const val PENDENTE_UTILIZACAO = "PENDENTE_UTILIZACAO"

    //documento cancelado no SAP - tanto o original estornado quanto o documento de
    //cancelamento que o anula (ver MapaRelacoesService.cancelado). O front tira os dois
    //do desenho do grafo e dos totais, e mostra a pilha num canto
    const val CANCELADO = "CANCELADO"
}

data class MapaNode(
    val id: String,
    val tipo: MapaTipoDocumento,
    val docEntry: Int?,
    val docNum: String?,
    val cardCode: String?,
    val label: String,
    val valor: BigDecimal?,
    val data: String?,
    val status: String?,
    //situacao destacada como etiqueta colorida no card (ver SITUACAO_BADGE no front) -
    //separado de status porque alguns tipos usam os dois (ex.: adiantamento tem
    //DocumentStatus proprio E pode estar pendente de utilizacao)
    val situacao: String? = null,
    //linhas extras do card, abaixo do numero (ex.: produto e quantidade da OP no rastreio de lote)
    val detalhes: List<String> = listOf(),
    //item do lote (no LOTE do rastreio): o numero do lote so e unico junto do item, e a tela manda os
    //dois para abrir o nivel seguinte
    val item: String? = null,
    //no LOTE do rastreio: a tela pode abrir o destino dele (quem consumiu, quem comprou)
    val destino: Boolean? = null
)

data class MapaEdge(
    val id: String,
    val from: String,
    val to: String,
    val tipo: TipoAresta,
    //texto escrito sobre a aresta (o numero do lote no rastreio)
    val rotulo: String? = null
)

class MapaRelacoesResponse(
    val root: String,
    val nodes: List<MapaNode>,
    val edges: List<MapaEdge>,
    //observacao exibida acima do grafo (ex.: lote comprado, sem origem para mostrar)
    val aviso: String? = null
)
