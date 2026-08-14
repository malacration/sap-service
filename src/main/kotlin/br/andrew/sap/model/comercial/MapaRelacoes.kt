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
    RECLASSIFICACAO
}

//valores de MapaNode.situacao que nao vem direto de um campo do SAP
object SituacaoNode {
    //adiantamento que ainda tem saldo nao apropriado por nenhuma nota fiscal
    const val PENDENTE_UTILIZACAO = "PENDENTE_UTILIZACAO"
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
    val situacao: String? = null
)

data class MapaEdge(
    val id: String,
    val from: String,
    val to: String,
    val tipo: TipoAresta
)

class MapaRelacoesResponse(
    val root: String,
    val nodes: List<MapaNode>,
    val edges: List<MapaEdge>
)
