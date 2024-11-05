package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.forca.Produto
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product

class PedidoTroca(
    val docEntry : String,
    val itemSaida : List<ItemTroca>,
    val itemRecebido : List<Product>
) {

}

class ItemTroca(
    val itemCode : String,
    val quantidade : Double,
    val precoNegociado : Double
)