package br.andrew.sap.model.self.venda

import java.util.Date

class ContratoVendaFutura(
    val idPedido : Int,
    val cardCode : String,
    val cardName : String,
    val dataCriacao : Date,
    val itens : List<Item>,
    val valorFrete  : Double,
) {
}


class Item()