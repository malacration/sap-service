package br.andrew.sap.model

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
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

    @Test
    fun condicaoIn(){
        val coluna = "Code";
        val comparador = Condicao.IN
        val valor = listOf<String>("francisco","gabriel")
        val resultado = Filter(listOf(Predicate(coluna,valor,comparador))).toString()
        Assertions.assertEquals("\$filter=(Code eq 'francisco' or Code eq 'gabriel')",resultado)
    }

    @Test
    fun condicaoContains(){
        val coluna = "Code";
        val comparador = Condicao.CONTAINS
        val valor = "geovana"
        val resultado = Filter(listOf(Predicate(coluna,valor,comparador))).toString()
        Assertions.assertEquals("\$filter=contains(Code, 'geovana')",resultado)
    }
}