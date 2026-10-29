package br.andrew.sap.model.producao

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Status da ordem de producao (ProductionOrderStatus do Service Layer). A listagem so aceita
 * estes valores no filtro: o texto vai direto pro $filter do OData, entao nada fora do enum passa.
 */
enum class StatusOrdemProducao(val sap: String) {
    PLANEJADA("boposPlanned"),
    LIBERADA("boposReleased"),
    FECHADA("boposClosed"),
    CANCELADA("boposCancelled");

    companion object {
        fun from(valor: String): StatusOrdemProducao =
            entries.firstOrNull { it.name.equals(valor, true) || it.sap.equals(valor, true) }
                ?: throw IllegalArgumentException("Status de ordem de produção inválido: $valor")
    }
}

/**
 * Ordem de producao completa, para a tela de detalhe e os formularios em PDF (ordem de producao e
 * ordem de embalagem). `U_LbrOne_Batelada` e o NUMERO de bateladas (ver SysfeedProductionOrderService).
 */
// Mantem os nomes do Service Layer (AbsoluteEntry...) no JSON da API; sem isso sai absoluteEntry e o front nao acha.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdemProducaoDetalhe(
    val AbsoluteEntry: Int? = null,
    val DocumentNumber: Int? = null,
    val ItemNo: String? = null,
    val ProductDescription: String? = null,
    val ProductionOrderStatus: String? = null,
    val ProductionOrderType: String? = null,
    val PlannedQuantity: Double? = null,
    val CompletedQuantity: Double? = null,
    val RejectedQuantity: Double? = null,
    val PostingDate: String? = null,
    val StartDate: String? = null,
    val DueDate: String? = null,
    val Warehouse: String? = null,
    val Remarks: String? = null,
    val U_LbrOne_Batelada: Double? = null,
    // Numero da ordem no SysFeed (preenchido na integracao).
    val U_sysfeed_numero: String? = null,
    // Lote do produto acabado (campo criado em ProducaoFieldsConfiguration).
    val U_lote_producao: String? = null,
    val ProductionOrderLines: List<OrdemProducaoLinha> = listOf(),
    // Dados do cadastro do produto acabado para os formularios; nao vem do Service Layer na ordem.
    val Produto: ProdutoDaOrdem? = null,
)

/**
 * Cadastro do produto da ordem (OITM + estrutura OITT). Na ordem de embalagem (PAC) o peso do saco
 * e a unidade de venda dizem quantos sacos sao; na de producao (PDC) a formula e a do SysFeed.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProdutoDaOrdem(
    val ValidadeDias: Int? = null,
    val PesoSaco: Double? = null,
    val UnidadeVenda: String? = null,
    val Formula: Int? = null,
)

// Mantem os nomes do Service Layer (AbsoluteEntry...) no JSON da API; sem isso sai absoluteEntry e o front nao acha.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdemProducaoLinha(
    val LineNumber: Int? = null,
    val ItemNo: String? = null,
    val ItemName: String? = null,
    // pit_Item (materia-prima), pit_Resource (recurso/maquina) ou pit_Text
    val ItemType: String? = null,
    val BaseQuantity: Double? = null,
    val PlannedQuantity: Double? = null,
    val IssuedQuantity: Double? = null,
    val Warehouse: String? = null,
)

/** Cabecalho da ordem de producao, sem as linhas (ProductionOrderLines), para a listagem. */
// Mantem os nomes do Service Layer (AbsoluteEntry...) no JSON da API; sem isso sai absoluteEntry e o front nao acha.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdemProducao(
    val AbsoluteEntry: Int? = null,
    val DocumentNumber: Int? = null,
    val Series: Int? = null,
    val ItemNo: String? = null,
    val ProductDescription: String? = null,
    val ProductionOrderStatus: String? = null,
    val ProductionOrderType: String? = null,
    val PlannedQuantity: Double? = null,
    val CompletedQuantity: Double? = null,
    val RejectedQuantity: Double? = null,
    val PostingDate: String? = null,
    val StartDate: String? = null,
    val DueDate: String? = null,
    val Warehouse: String? = null,
    val Remarks: String? = null,
    // Numero de bateladas: a ordem de embalagem em lote sai da listagem, sem abrir cada ordem.
    val U_LbrOne_Batelada: Double? = null,
    val U_sysfeed_numero: String? = null,
    val U_lote_producao: String? = null,
)
