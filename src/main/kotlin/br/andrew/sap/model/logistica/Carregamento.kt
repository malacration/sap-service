package br.andrew.sap.model.logistica

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.Date

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Carregamento(
        val DocEntry: Int?,
        val U_nameOrdem: String?,
        var U_Status: String?,
        val CreateDate : String = Date().toInstant().toString(),
        val U_filial : Int?,
        val U_placa : String?,
        val U_motorista : String?,
        val U_capacidadeCaminhao : Double?,
        val U_transportadora : String?
){
        var Quantity : Double? = null
        var Weight1 : Double? = null
        var docEntryQuantity : Int? = null
        //Quem fez e o que fez na ultima gravacao (cada versao fica no log do UDO).
        var U_usuario : String? = null
        var U_acao : String? = null

        //Mesmo cuidado do CFOPCode em DocumentLines: nome fixado nos dois acessores, senao o
        //UpperCamelCase gera uma segunda propriedade ("Ord_CRG_LINHACollection").
        @get:JsonProperty("ORD_CRG_LINHACollection")
        @set:JsonProperty("ORD_CRG_LINHACollection")
        var ORD_CRG_LINHACollection : List<CarregamentoLinha>? = null

        fun retiradasDeFatura() : List<CarregamentoLinha> =
                ORD_CRG_LINHACollection.orEmpty().filter { it.U_baseType == CarregamentoLinha.FATURA_RESERVA }
}

/**
 * Linha da @ORD_CRG_LINHA. Hoje so guarda retirada parcial de fatura de reserva (simples
 * faturamento): pedido continua vinculado pelo U_ORD_CARREGAMENTO da RDR1, linha inteira.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class CarregamentoLinha(
        val U_baseType : Int? = FATURA_RESERVA,
        val U_docEntryFatura : Int? = null,
        val U_baseLine : Int? = null,
        val U_qtdRetirada : Double? = null,
        val U_itemCode : String? = null,
        val U_cardCode : String? = null,
){
        companion object {
                const val FATURA_RESERVA = 13
        }
}