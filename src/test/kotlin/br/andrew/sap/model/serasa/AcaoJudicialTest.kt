package br.andrew.sap.model.serasa

import br.andrew.sap.model.serasa.retorno.AcaoJudicial
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class AcaoJudicialTest {

    @Test
    fun test(){
        val entrada = "I120002019011020190110000000001000000019204599SANTOS                        000000019204599                        "
        val acaoJudicial = AcaoJudicial(entrada)
        Assertions.assertEquals(SimpleDateFormat("yyyyMMdd").parse("20190110"), acaoJudicial.dataInicial)
        Assertions.assertEquals(SimpleDateFormat("yyyyMMdd").parse("20190110"), acaoJudicial.dataFinal)
        Assertions.assertEquals("000000001", acaoJudicial.quantidade)
        Assertions.assertEquals(192045.99, acaoJudicial.valor)
        Assertions.assertEquals("SANTOS                        ", acaoJudicial.origem)
    }

}