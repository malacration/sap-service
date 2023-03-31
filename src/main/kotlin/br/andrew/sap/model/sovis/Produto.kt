package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Produto(val idProduto : String,
              val precoUnitario : Double,
              val quantidade : Double){

    var desconto : Double = 0.0
    @JsonIgnore
    fun getProduct(tipoPedido: Int): Product {
        return Product(idProduto,quantidade.toString(),precoUnitario.toString(),tipoPedido)
                .also { it.discountPercent = desconto }
    }

}