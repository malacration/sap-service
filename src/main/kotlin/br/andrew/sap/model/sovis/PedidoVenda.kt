package br.andrew.sap.model.sovis

import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class PedidoVenda(
        val idCliente : String,
        val idEmpresa : String,
        val idFormaPagamento: String,
        val idCondicaoPagamento : String,
        @JsonFormat(with = arrayOf(JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
        val produtos : List<Produto>,
        val codVendedor : Int) {

    var idPedido : String? = null

    //TODO fazer parse de data
    var dataEntraga : String = SimpleDateFormat("yyy-MM-dd").format(Date())
    var tipoPedido : String = "9"
    var desconto : Double = 0.0


    @JsonIgnore
    fun getOrder(): OrderSales {
        return OrderSales(idCliente, dataEntraga,
                produtos.map { it.getProduct() },idEmpresa,tipoPedido)
                .also {
                    it.paymentMethod = idFormaPagamento
                    it.discountPercent = desconto
                    it.paymentGroupCode = idCondicaoPagamento
                    it.salesPersonCode = codVendedor
                }
    }
}

