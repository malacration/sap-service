package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Comissao(val Code : Int, val U_porcentagem : Double) {

    var descontoMaximo : Double? = null
    var descontoRegressivo : List<DescontoRegressivo> = listOf()
    var liberadoPara : List<LiberadoPara> = listOf() // Se vazio libera para geral
    var prazoPagamento : List<PrazoPagamentoDto> = listOf()

    fun getTipoDesconto() : TipoDesconto{
        return if(descontoRegressivo.size == 0){
            TipoDesconto.DescontoAbsoluto
        }else{
            TipoDesconto.DescontoRegressivo
        }
    }
}


//TODO nao pode existir comissao sem Prazo Pagamento

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class PrazoPagamento(){
    // idPrazoReal-idTabela
    val prazoPagamento = "LinkObjetoSAP"
    val desconto = ""
    val juros = ""
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class LiberadoPara(){
    var filial : String = "";
    var vendedor : String = "";
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class DescontoRegressivo(val U_porcentagemDesconto : Double, val U_porcentagemComissao : Double)

enum class TipoDesconto(){
    DescontoRegressivo,
    DescontoAbsoluto //Esse desconto reduz o valor da comissao paga em razao do valor negociado

}