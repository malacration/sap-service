package br.andrew.sap.model.sovis

class PedidoVenda(
        val idCliente : String,
        val idEmpresa : String,
        val idFormaPagamento: String,
        val idCondicaoPagamento : String,
        val produtos : List<Produto>,
        val codVendedor : String) {

}

class Produto(val codProduto : String,
              val precoUnitario : Double,
              val quantidade : Double){

}