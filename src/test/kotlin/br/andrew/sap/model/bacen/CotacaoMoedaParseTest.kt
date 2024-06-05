package br.andrew.sap.model.bacen

import br.andrew.sap.services.bacen.CotacaoMoedaParse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class CotacaoMoedaParseTest {

    val dia1 = SimpleDateFormat("yyyy-MM-dd").parse("2022-06-01")
    val dia2 = SimpleDateFormat("yyyy-MM-dd").parse("2022-06-02")
    val dia3 = SimpleDateFormat("yyyy-MM-dd").parse("2022-06-03")

    @Test
    fun cotacoesEmBranco(){
        val cotacoes = listOf<CotacaoMoeda>()
        val result = CotacaoMoedaParse().toCurrencyRateRange(cotacoes)
        Assertions.assertEquals(0,result.size)
    }

    @Test
    fun cotacoesNormal(){
        val cotacoes = mutableListOf<CotacaoMoeda>()
        cotacoes.add(CotacaoMoeda(5.0,5.0,dia1,"final"))
        val result = CotacaoMoedaParse().toCurrencyRateRange(cotacoes)
        Assertions.assertEquals(1,result.size)
    }

    @Test
    fun cotacaoCom3Datas(){
        val cotacoes = mutableListOf<CotacaoMoeda>()
        cotacoes.add(CotacaoMoeda(5.0,5.0,dia1,"final"))
        cotacoes.add(CotacaoMoeda(5.0,5.0,dia2,"final"))
        cotacoes.add(CotacaoMoeda(5.0,5.0,dia3,"final"))
        val result = CotacaoMoedaParse().toCurrencyRateRange(cotacoes)
        Assertions.assertEquals(3,result.size)
    }

    @Test
    fun cotacaoCom2DatasMasUmDiaFaltante(){
        val cotacoes = mutableListOf<CotacaoMoeda>()
        cotacoes.add(CotacaoMoeda(4.0,4.0,dia1,"final"))
        cotacoes.add(CotacaoMoeda(5.0,5.0,dia3,"final"))
        val result = CotacaoMoedaParse().toCurrencyRateRange(cotacoes)
        Assertions.assertEquals(3,result.size)
        Assertions.assertEquals("4.0",result[1].Rate)
        Assertions.assertEquals("2022-06-02",result[1].RateDate)
    }


    @Test
    fun cotacaoComFuturo(){
        val cotacoes = mutableListOf<CotacaoMoeda>()
        cotacoes.add(CotacaoMoeda(4.0,4.0,dia1,"final"))
        val result = CotacaoMoedaParse().toCurrencyRateRange(cotacoes,null,10)
        Assertions.assertEquals(11,result.size)
        Assertions.assertEquals("4.0",result[10].Rate)
        Assertions.assertEquals("2022-06-11",result[10].RateDate)
    }


}