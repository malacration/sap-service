package br.andrew.sap.model.forca

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.pricing.ComissaoService
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
        var idPedido : String,
        val codVendedor : Int = -1) {

    var frete: Double? = null
    var observacao : String? = null
    var precoBase : Int? = null
    var endereco : String? = null
    var uuid : String? = null

    //TODO fazer parse de data
    var dataEntraga : String? = SimpleDateFormat("yyy-MM-dd").format(Date())
        set(value)  {
            if(value != null)
                field = value
        }
    var tipoPedido : Int = 9
    var desconto : Double = 0.0

    @JsonIgnore
    fun getOrder(itemService: ItemsService, comissaoService: ComissaoService): OrderSales {
        return build(
            OrderSales(idCliente,dataEntraga,
            produtos.map { it.getProduct(tipoPedido,itemService,comissaoService) }, idEmpresa)
        )

    }

    @JsonIgnore
    fun getQuotation(itemService: ItemsService, comissaoService: ComissaoService): Quotation {
        return build(
            Quotation(idCliente, dataEntraga,
            produtos.map { it.getProduct(tipoPedido,itemService,comissaoService) },idEmpresa)
        )
    }

    fun <T : Document> build(document : T) : T{
        return document.also {
            val condicao = idCondicaoPagamento.split("_")
            it.paymentMethod = idFormaPagamento
            it.discountPercent = desconto
            it.paymentGroupCode = if(condicao.size > 1)
                condicao[1].toIntOrNull() ?: throw Exception("Erro ao converter para inteiro")
            else
                idCondicaoPagamento.toIntOrNull() ?: throw Exception("Erro ao converter para inteiro")
            try {
                if(endereco != null)
                    it.shipToCode = getEnderecoEntrega().code
            }catch (e : Exception ){
                e.printStackTrace()
            }
                it.salesPersonCode = codVendedor
            it.u_pedido_update = "1"
            it.comments = observacao
            it.OpeningRemarks = observacao
            it.u_id_pedido_forca = idPedido
            it.u_uuid_forca = uuid
            if(frete != null)
                it.documentAdditionalExpenses.add(AdditionalExpenses.frete(frete!!))
        }
    }

    @JsonIgnore
    fun getEnderecoEntrega(): EnderecoId {
        return EnderecoId(endereco!!)
    }


}

