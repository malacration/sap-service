package br.andrew.sap.rovema.model

import br.andrew.sap.rovema.infrastructure.odata.Condicao
import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.Predicate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FilterTest {

    @Test
    fun test(){
        val resultado = Filter().toString()
        Assertions.assertEquals("",resultado)
    }

    @Test
    fun startsWith(){
        val coluna = "Code";
        val comparador = Condicao.STARTS_WITH
        val valor = "a"
        val resultado = Filter(listOf(Predicate(coluna,valor,comparador))).toString()
        Assertions.assertEquals("\$filter=startswith(Code, 'a')",resultado)
    }
}