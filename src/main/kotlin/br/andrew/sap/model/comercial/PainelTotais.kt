package br.andrew.sap.model.comercial

/** Totais do filtro do painel integrado, sem quantidade: pedido e NF misturam unidades. */
data class PainelTotais(val linhas: Long, val pedidos: Long, val notasVendaFutura: Long)
