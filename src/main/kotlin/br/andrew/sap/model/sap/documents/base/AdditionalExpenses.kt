package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AdditionalExpenses(val expenseCode : Int, var LineTotal : Double){

    var distributionMethod : String = "aedm_RowTotal"
    var TaxType : String? = null;
    var LineNum : Int? = null
    var DistributionRule : String? = null
    var DistributionRule2 : String? = null

    //Codigo de imposto da despesa, determinado pelo SAP. So vem preenchido quando o documento
    //e lido de volta do Service Layer - e o que o DesoneradoService usa para descobrir se essa
    //despesa tem ICMS desonerado.
    //Entra na desserializacao (setter) e nunca sai no JSON (getter ignorado): devolver o TaxCode
    //da despesa num PATCH faz o Service Layer responder "Internal error (-5002)". Precisa ser nos
    //acessores - @JsonProperty(access = WRITE_ONLY) na propriedade Kotlin nao suprime a escrita.
    @get:JsonIgnore
    @set:JsonProperty("TaxCode")
    var TaxCode : String? = null

    //Frete negociado com o cliente (UDF U_frete_negociado em INV3/RDR3/QUT3/RIN3). E o alvo:
    //quando ha ICMS desonerado o LineTotal e majorado a partir daqui para que o liquido volte
    //a esse valor. Vazio ou zero = documento anterior a essa funcionalidade, nada e recalculado.
    var U_frete_negociado : Double? = null

    @JsonIgnore
    fun temFreteNegociado() : Boolean {
        return (U_frete_negociado ?: 0.0) > 0.0
    }

    //Valor do frete para efeito de conferencia: o negociado quando existe, senao o que esta
    //lancado na despesa. Mesma regra do bloco de frete da SBO_SP_VALIDACAO_VENDA_FUTURA.
    @JsonIgnore
    fun valorConferencia() : Double {
        return if(temFreteNegociado()) U_frete_negociado!! else LineTotal
    }

//    val lineTotal : Double = lineTotalSys
//    val LineGross : Double = lineTotalSys
//    val LineGrossSys : Double = lineTotalSys

    fun setDistribuicaoCusto(custoByBranch: DistribuicaoCustoByBranch) {
        DistributionRule = custoByBranch.grupoEconomico
        DistributionRule2 = custoByBranch.centroCusto
    }

    companion object{
        //ExpnsCode 1 = frete (INV3/RIN3/QUT3."ExpnsCode"), mesma constante usada pela
        //SBO_SP_VALIDACAO_VENDA_FUTURA para apurar o frete de venda futura.
        const val CODIGO_FRETE = 1

        //Toda entrada de pedido passa por aqui - sovis/forca de vendas (PedidoVenda.build) e
        //angular (Document.frete) - entao o valor negociado e preenchido em um lugar so.
        @JsonIgnore
        fun frete(valor : Double) : AdditionalExpenses {
            return AdditionalExpenses(CODIGO_FRETE,valor).also { it.U_frete_negociado = valor }
        }
    }
}