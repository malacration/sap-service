package br.andrew.sap.model.sap.cadastro
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Regiao {

    var Code : String? = null
    var Name : String? = null

    @JsonProperty("U_NomeRegiao")
    var U_NomeRegiao : String? = null

    @JsonProperty("U_CodCordenador")
    var U_CodCordenador : String? = null

    //BPLID da filial (@BusinessPlaces), uma unica filial por regiao
    @JsonProperty("U_Filial")
    var U_Filial : Int? = null

    //TESTE REGIAO2: nome da colecao trocado temporariamente pro clone "Regiao2"
    //(ver RegiaoService.path() e Regiao2TesteConfiguration.kt). Reverter pra
    //"RO_REGIAO_LINHASCollection" quando terminar o diagnostico.
    //ALWAYS (em vez de herdar o NON_EMPTY da classe): precisa ir no JSON mesmo
    //quando a lista fica vazia, senao o service layer nunca fica sabendo que
    //a ultima linha deve ser removida.
    @JsonProperty("RO_REGIAO2_LINHASCollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var linhas : MutableList<RegiaoLinha> = mutableListOf()

    //faixas progressivas de preco por km, por quantidade de itens - especifica dessa regiao
    //TESTE REGIAO2: idem acima, reverter pra "RO_REGIAO_FAIXACollection"
    @JsonProperty("RO_REGIAO2_FAIXACollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var faixas : MutableList<RegiaoFaixa> = mutableListOf()

    fun temLocalidade(codLocal : String) : Boolean {
        return linhas.any { it.U_Locais == codLocal }
    }

    fun addLocalidade(codLocal : String, distanciaKm : Double?) : Regiao {
        if(temLocalidade(codLocal))
            throw Exception("A localidade $codLocal ja esta vinculada a regiao $Code")
        linhas.add(RegiaoLinha(Code, proximoLineId(linhas.mapNotNull { it.LineId }), codLocal, distanciaKm))
        return this
    }

    fun removeLocalidade(codLocal : String) : Regiao {
        if(!temLocalidade(codLocal))
            throw Exception("A localidade $codLocal nao esta vinculada a regiao $Code")
        linhas = linhas.filter { it.U_Locais != codLocal }.toMutableList()
        return this
    }

    fun atualizaDistancia(codLocal : String, distanciaKm : Double) : Regiao {
        val linha = linhas.firstOrNull { it.U_Locais == codLocal }
            ?: throw Exception("A localidade $codLocal nao esta vinculada a regiao $Code")
        linha.U_Distancia = distanciaKm
        return this
    }

    fun addFaixa(qtdeMinima : Int, valorKm : Double) : Regiao {
        validaQtdeMinima(qtdeMinima)
        if(faixas.any { it.U_QtdeAte == qtdeMinima })
            throw Exception("Ja existe uma faixa com quantidade minima $qtdeMinima")
        faixas.add(RegiaoFaixa(Code, proximoLineId(faixas.mapNotNull { it.LineId }), qtdeMinima, valorKm))
        return this
    }

    fun removeFaixa(lineId : Int) : Regiao {
        if(faixas.none { it.LineId == lineId })
            throw Exception("Faixa de preco nao encontrada")
        faixas = faixas.filter { it.LineId != lineId }.toMutableList()
        return this
    }

    fun atualizaFaixa(lineId : Int, qtdeMinima : Int, valorKm : Double) : Regiao {
        validaQtdeMinima(qtdeMinima)
        val faixa = faixas.firstOrNull { it.LineId == lineId }
            ?: throw Exception("Faixa de preco nao encontrada")
        if(faixas.any { it.LineId != lineId && it.U_QtdeAte == qtdeMinima })
            throw Exception("Ja existe uma faixa com quantidade minima $qtdeMinima")
        faixa.U_QtdeAte = qtdeMinima
        faixa.U_ValorKm = valorKm
        return this
    }

    private fun validaQtdeMinima(qtdeMinima : Int) {
        if(qtdeMinima < 0)
            throw Exception("A quantidade minima nao pode ser negativa")
    }

    private fun proximoLineId(existentes : List<Int>) : Int {
        return (existentes.maxOrNull() ?: 0) + 1
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RegiaoLinha(var Code : String? = null,
                  var LineId : Int? = null,
                  @JsonProperty("U_Locais")
                  var U_Locais : String? = null,
                  @JsonProperty("U_Distancia")
                  var U_Distancia : Double? = null)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RegiaoFaixa(var Code : String? = null,
                  var LineId : Int? = null,
                  //o campo no SAP se chama U_QtdeAte mas hoje representa a
                  //QUANTIDADE MINIMA pra faixa valer (obrigatorio, nunca nulo).
                  //ex.: 0 = sempre disponivel; 100 = so a partir de 100 itens.
                  //a faixa aplicada e a de maior minimo que a quantidade atinge
                  @JsonProperty("U_QtdeAte")
                  var U_QtdeAte : Int? = null,
                  @JsonProperty("U_ValorKm")
                  var U_ValorKm : Double? = null)
