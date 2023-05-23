package br.andrew.sap.model.forca

import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Produto(val idProduto : String,
              val valorTabela : Double,
              val quantidade : Double){

    var desconto : Double = 0.0
    var precoUnitario : Double = 0.0
    var idItemForca: String? = null


    @JsonIgnore
    fun getProduct(tipoPedido: Int): Product {
        return Product(idProduto,quantidade.toString(),valorTabela.toString(),tipoPedido)
                .also {
                    it.discountPercent = desconto
                    it.U_preco_negociado = precoUnitario
                    it.U_id_item_forca = idItemForca
                }
    }

}