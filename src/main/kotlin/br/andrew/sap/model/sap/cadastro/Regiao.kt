package br.andrew.sap.model.sap.cadastro
import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Regiao : BatchId {

    var Code : String? = null
    var Name : String? = null

    @JsonProperty("U_NomeRegiao")
    var U_NomeRegiao : String? = null

    @JsonProperty("U_CodCordenador")
    var U_CodCordenador : String? = null

    //BPLID da filial (@BusinessPlaces) - varias regioes podem compartilhar a
    //mesma filial (ex.: regiao normal e uma promocional), mas so uma delas
    //pode estar ativa por filial ao mesmo tempo (ver RegiaoService.ativar)
    @JsonProperty("U_Filial")
    var U_Filial : Int? = null

    //custo por unidade pra trazer o produto da fabrica ate a unidade/filial dessa
    //regiao. E somado ao valor por unidade da faixa antes de multiplicar pela
    //quantidade (ver calcularFrete), entao acompanha o volume do pedido.
    //Regiao cadastrada antes desse campo vem nula - vale 0
    @JsonProperty("U_CustoTransporte")
    var U_CustoTransporte : Double? = null

    //"0"/"1" (padrao do UDF de checkbox no service layer, ver VendedorConfiguration).
    //toda regiao nova comeca desativada - so passa a valer quando ativada
    @JsonProperty("U_Ativa")
    var U_Ativa : String? = null

    @get:JsonIgnore
    val ativa : Boolean
        get() = U_Ativa == "1"

    //ALWAYS (em vez de herdar o NON_EMPTY da classe): precisa ir no JSON mesmo
    //quando a lista fica vazia, senao o service layer nunca fica sabendo que
    //a ultima linha deve ser removida.
    @JsonProperty("AR_REGIAO_LINHASCollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var linhas : MutableList<RegiaoLinha> = mutableListOf()

    //faixas progressivas de preco a cada 100km, por quantidade de itens - especifica dessa regiao
    @JsonProperty("AR_REGIAO_FAIXACollection")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    var faixas : MutableList<RegiaoFaixa> = mutableListOf()

    //usado pelo BatchService pra montar a URL da requisicao dentro do changeset
    //(ver RegiaoService.substituir)
    @JsonIgnore
    override fun getId() : String = "'$Code'"

    fun temLocalidade(codLocal : String) : Boolean {
        return linhas.any { it.U_Locais == codLocal }
    }

    fun getDistancia(codLocal : String) : Double? {
        return linhas.firstOrNull { it.U_Locais == codLocal }?.U_Distancia
    }

    //faixa aplicada e a de maior quantidade minima que a quantidade ainda atinge
    //(desconto progressivo por volume) - espelha Regiao.encontraFaixa no front
    fun encontraFaixa(quantidade : Double) : RegiaoFaixa? {
        return faixas
            .filter { it.U_QtdeAte != null && quantidade >= it.U_QtdeAte!! }
            .maxByOrNull { it.U_QtdeAte!! }
    }

    /**
     * Faixa base: a de menor quantidade minima. A quantidade dela e o minimo para a regiao entregar
     * (ex.: base 50 = so entrega pedido de 50 unidades para cima). null = regiao sem faixa.
     */
    fun faixaBase() : RegiaoFaixa? = faixas.filter { it.U_QtdeAte != null }.minByOrNull { it.U_QtdeAte!! }

    fun quantidadeMinimaEntrega() : Int? = faixaBase()?.U_QtdeAte

    //mesma formula do simulador de frete no front (Regiao.calcularFrete em
    //regiao.ts) - usada pra revalidar no backend o frete calculado pelo
    //front antes de gravar a venda (ver DocumentForAngular).
    //quantidadePedido: o minimo de entrega vale para o pedido inteiro. Quando o pedido se divide em
    //documentos (por condicao de pagamento), um documento pode ficar abaixo da base - ele usa a faixa
    //base, desde que o pedido inteiro a alcance.
    fun calcularFrete(codLocal : String, quantidade : Double, quantidadePedido : Double = quantidade) : Double? {
        val distancia = getDistancia(codLocal) ?: return null
        val faixa = encontraFaixa(quantidade)
            ?: faixaBase()?.takeIf { maxOf(quantidade, quantidadePedido) >= it.U_QtdeAte!! }
            ?: return null
        val valorKm = faixa.U_ValorKm ?: return null
        return ((distancia / 100.0) * valorKm + (U_CustoTransporte ?: 0.0)) * quantidade
    }

    /** Pedido com entrega abaixo da base da regiao: a regiao nao entrega, a venda tem que ser retirada. */
    fun validaQuantidadeMinimaEntrega(quantidadePedido : Double) {
        val minima = quantidadeMinimaEntrega() ?: return
        if(quantidadePedido < minima)
            throw Exception("A quantidade mínima para entrega nesta região é $minima unidades " +
                "(o pedido tem ${quantidadeTexto(quantidadePedido)}). Para menos, use Retirada.")
    }

    private fun quantidadeTexto(valor : Double) = java.math.BigDecimal(valor).stripTrailingZeros().toPlainString()

    fun addLocalidade(codLocal : String, distanciaKm : Double) : Regiao {
        if(temLocalidade(codLocal))
            throw Exception("A localidade $codLocal ja esta vinculada a regiao $Code")
        if(distanciaKm < 0)
            throw Exception("A distancia nao pode ser negativa")
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

    //a menor quantidade cadastrada e a base (minimo para entregar) - ver faixaBase
    fun addFaixa(qtdeMinima : Int, valorKm : Double) : Regiao {
        validaQtdeMinima(qtdeMinima)
        if(faixas.any { it.U_QtdeAte == qtdeMinima })
            throw Exception("Ja existe uma faixa com quantidade minima $qtdeMinima")
        validaOrdemPreco(qtdeMinima, valorKm)
        faixas.add(RegiaoFaixa(Code, proximoLineId(faixas.mapNotNull { it.LineId }), qtdeMinima, valorKm))
        return this
    }

    fun removeFaixa(lineId : Int) : Regiao {
        faixas.firstOrNull { it.LineId == lineId }
            ?: throw Exception("Faixa de preco nao encontrada")
        //remover a base e permitido: a proxima faixa vira a base e o minimo de entrega sobe junto
        faixas = faixas.filter { it.LineId != lineId }.toMutableList()
        return this
    }

    fun atualizaFaixa(lineId : Int, qtdeMinima : Int, valorKm : Double) : Regiao {
        validaQtdeMinima(qtdeMinima)
        val faixa = faixas.firstOrNull { it.LineId == lineId }
            ?: throw Exception("Faixa de preco nao encontrada")
        //mudar a quantidade da base muda o minimo de entrega da regiao (ex.: 1 -> 50)
        if(faixas.any { it.LineId != lineId && it.U_QtdeAte == qtdeMinima })
            throw Exception("Ja existe uma faixa com quantidade minima $qtdeMinima")
        validaOrdemPreco(qtdeMinima, valorKm, lineId)
        faixa.U_QtdeAte = qtdeMinima
        faixa.U_ValorKm = valorKm
        return this
    }

    private fun validaQtdeMinima(qtdeMinima : Int) {
        if(qtdeMinima < 1)
            throw Exception("A quantidade minima deve ser pelo menos 1")
    }

    /**
     * Desconto progressivo: quanto maior a quantidade minima da faixa, menor
     * (ou igual) deve ser o valor - senao comprar mais sairia mais caro que
     * comprar menos. lineIdIgnorado exclui a propria faixa (edicao) da
     * comparacao com ela mesma.
     */
    private fun validaOrdemPreco(qtdeMinima : Int, valorKm : Double, lineIdIgnorado : Int? = null) {
        val ordenadas = faixas.filter { it.LineId != lineIdIgnorado }.sortedBy { it.U_QtdeAte }
        val anterior = ordenadas.lastOrNull { (it.U_QtdeAte ?: 1) < qtdeMinima }
        val proxima = ordenadas.firstOrNull { (it.U_QtdeAte ?: 1) > qtdeMinima }
        if(anterior != null && valorKm > (anterior.U_ValorKm ?: 0.0))
            throw Exception("O valor nao pode ser maior que o da faixa anterior (quantidade minima ${anterior.U_QtdeAte}, valor ${anterior.U_ValorKm})")
        if(proxima != null && valorKm < (proxima.U_ValorKm ?: 0.0))
            throw Exception("O valor nao pode ser menor que o da proxima faixa (quantidade minima ${proxima.U_QtdeAte}, valor ${proxima.U_ValorKm})")
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
                  //ex.: 1 = cobre a partir do 1o item; 100 = so a partir de 100
                  //itens. a faixa aplicada e a de maior minimo que a quantidade
                  //atinge; a de menor minimo e a base (minimo para entregar)
                  @JsonProperty("U_QtdeAte")
                  var U_QtdeAte : Int? = null,
                  @JsonProperty("U_ValorKm")
                  var U_ValorKm : Double? = null)
