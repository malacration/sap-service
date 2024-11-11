package br.andrew.sap.model.forca

import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.pricing.ComissaoService
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Produto(val idProduto : String,
              val valorTabela : Double,
              val quantidade : Double,
              var listapreco : Int){

    var desconto : Double = 0.0
    var precoUnitario : Double = 0.0
    var idItem: String? = null
    var ItemDescription : String? = null
    var MeasureUnit : String? = null

    constructor(ItemCode : String, valorTabela : Double, quantidade : Double, listapreco : Int, none : String = "0") :
            this(ItemCode,valorTabela,quantidade,listapreco)


    //TODO criar uma classe que faz essa atividade para manter o padrao.
    @JsonIgnore
    fun getProduct(tipoPedido: Int, itemService: ItemsService, comissaoService: ComissaoService): Product {
        val precoBase = itemService.getPriceBase(idProduto,listapreco)
        val comissao = comissaoService.getByIdTabela(listapreco)
        return Product(idProduto,quantidade.toString(),valorTabela.toString(),tipoPedido)
                .also {
                    it.DiscountPercent = desconto
                    it.U_preco_negociado = precoUnitario
                    it.U_id_item_forca = idItem
                    it.aplicaBase(precoBase,listapreco,comissao)
                    it.ItemDescription = ItemDescription
                    it.MeasureUnit = MeasureUnit
                }
    }

}