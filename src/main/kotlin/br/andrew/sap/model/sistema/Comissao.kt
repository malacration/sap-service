package br.andrew.sap.model.sistema
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//Code e a chave do UDO no service layer: alfanumerica, nao numerica. Ja existe
//comissao cadastrada com codigo "2,5" (a virgula vem do proprio nome da regra),
//entao tipar como Int quebrava a leitura da lista inteira
class Comissao(val Code : String, var U_porcentagem : Double) {

    var Name : String? = null

    //"0"/"1" (padrao do UDF de checkbox no service layer, ver Regiao.U_Ativa)
    @JsonProperty("U_regressiva")
    var U_regressiva : String? = null

    //desconto maximo (%) que o vendedor pode dar numa venda vinculada a essa comissao
    @JsonProperty("U_desconto")
    var U_desconto : Double? = null

    @JsonProperty("CONDICOESFVCollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var condicoesFV : MutableList<CondicaoComissao> = mutableListOf()

    @JsonProperty("LIBERAPARACollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var liberadoPara : MutableList<LiberadoPara> = mutableListOf()
}

//linha de condicoesFV: ajuste de desconto/juros por condicao de pagamento (U_prazo -> OCTG.GroupNum),
//especifico dessa comissao
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CondicaoComissao(
    var Code : String? = null,
    var LineId : Int? = null,
    @JsonProperty("U_prazo")
    var U_prazo : Int? = null,
    @JsonProperty("U_desconto")
    var U_desconto : Double? = null,
    @JsonProperty("U_juros")
    var U_juros : Double? = null,
)

//linha de LiberaPara: quem (filial/vendedor) pode usar essa comissao/tabela de preco
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LiberadoPara(
    var Code : String? = null,
    var LineId : Int? = null,
    @JsonProperty("U_Filial")
    var U_Filial : String? = null,
    @JsonProperty("U_vendedor")
    var U_vendedor : String? = null,
)
