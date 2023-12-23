package br.andrew.sap.model.serasa.retorno

import com.fasterxml.jackson.annotation.JsonIgnore

class ParseRetorno(@JsonIgnore val retorno : String) {

    val blocos = mutableListOf<String>()

    var blocsTypes : List<String>? = null

    init {
        val tamanho = retorno.length-400
        val numBlocos = tamanho/115

        for(i in 0..numBlocos-1){
            blocos.add(retorno.substring(400+(i*115), 400+((i+1)*115)))
        }
        blocsTypes = blocos.groupBy { it.substring(0,4) }.map { it.key }.toList().filter {
            it != "I110"
            && it != "I220"
            && it != "P002"
            && it != "I001"
            && it != "I140"
            && it != "I160"
            && it != "I170"
            && it != "I130"
            && it != "I120"
            && it != "A900"
            && it != "I230"
            && it != "I150"
        }
    }


    fun getNadaConsta() : List<String> {
        return blocos.filter { it.startsWith("A90000") }.map { it.substring(42,43+70) }
    }

    fun getProtestos() : List<Protestos> {
        return blocos.filter { it.startsWith("I11000") }.map { Protestos(it) }
    }

    fun getPendenciaComercial(): List<PendenciaComercial> {
        return blocos.filter { it.startsWith("I22000") }.map { PendenciaComercial(it) }
    }

    fun getPendenciaBancaria(): List<PendenciaBancaria> {
        return blocos.filter { it.startsWith("I14000") }.map { PendenciaBancaria(it) }
    }

    fun getChequeSemFundo(): List<ChequeSemFundo> {
        //TODO - Implementar testes - 160 e 170

        return blocos.filter { it.startsWith("I16000") }.map { ChequeSemFundo(it) }
            .toMutableList().also { it.addAll(
                blocos.filter { it.startsWith("I17000") }.map { ChequeSemFundo(it) }.toList()
            )}
    }

    fun getAcaoJudicial(): List<AcaoJudicial> {
        return blocos.filter { it.startsWith("I12000") }.map { AcaoJudicial(it) }
    }

    fun getFalenciaConcordata(): List<FalenciaConcordata> {
        return blocos.filter { it.startsWith("I15000") }.map { FalenciaConcordata(it) }
    }

    fun getScoreRshi() : List<ScoreRshi>{
        return blocos.filter { it.startsWith("F900RSHI001") }.map { ScoreRshi(it) }
    }

    fun getConvemDevedores() : List<ConvemDevedores>{
        return blocos.filter { it.startsWith("I23000") }.map { ConvemDevedores(it) }
    }

}