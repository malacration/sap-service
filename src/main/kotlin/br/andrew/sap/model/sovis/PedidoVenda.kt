package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.AdditionalExpenses
import br.andrew.sap.model.documents.OrderSales
import com.fasterxml.jackson.annotation.*
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
    var observacao : String? = null

    //TODO fazer parse de data
    var dataEntraga : String? = SimpleDateFormat("yyy-MM-dd").format(Date())
        set(value)  {
            if(value != null)
                field = value
        }
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
                    it.comments = observacao
                    it.u_id_pedido_forca = idPedido
                    if(this.frete != null)
                        it.documentAdditionalExpenses = listOf(AdditionalExpenses.frete(this.frete!!))
                }
    }
}

