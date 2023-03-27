package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

class PedidoVenda(
        val idCliente : String,
        val idEmpresa : String,
        val idFormaPagamento: String,
        val idCondicaoPagamento : String,
        val produtos : List<Produto>,
        val codVendedor : String) {

    @JsonIgnore
    fun getOrder(): OrderSales {
        return OrderSales(idCliente, Date(),produtos.map { it.getProduct() },idEmpresa,"62")
    }
}

class Produto(val idProduto : String,
              val precoUnitario : Double,
              val quantidade : Double){

    fun getProduct(): Product {
        return Product(idProduto,quantidade.toString(),precoUnitario.toString())
    }

}