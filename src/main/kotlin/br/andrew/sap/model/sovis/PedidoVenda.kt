package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.AdditionalExpenses
import br.andrew.sap.model.documents.OrderSales
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.text.SimpleDateFormat
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class PedidoVenda(
        val idCliente : String,
        val idEmpresa : String,
        val idFormaPagamento: String,
        val idCondicaoPagamento : String,
        @JsonFormat(with = arrayOf(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
        val produtos : List<Produto>,
        val codVendedor : Int = -1) {

    var frete: Double? = null
    var idPedido : String? = null

    //TODO fazer parse de data
    var dataEntraga : String = SimpleDateFormat("yyy-MM-dd").format(Date())
    var tipoPedido : Int = 9
    var desconto : Double = 0.0


    @JsonIgnore
    fun getOrder(): OrderSales {
        return OrderSales(idCliente, dataEntraga,
                produtos.map { it.getProduct(tipoPedido) },idEmpresa)
                .also {
                    it.paymentMethod = idFormaPagamento
                    it.discountPercent = desconto
                    it.paymentGroupCode = idCondicaoPagamento
                    it.salesPersonCode = codVendedor
                    it.u_pedido_update = "1"
                    if(this.frete != null)
                        it.documentAdditionalExpenses = listOf(AdditionalExpenses.frete(this.frete!!))
                }
    }
}

