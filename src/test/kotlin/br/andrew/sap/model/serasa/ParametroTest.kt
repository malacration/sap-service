package br.andrew.sap.model.serasa

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParametroTest {

    @Test
    fun test(){
        val resultado = "windson   "
        val parametro = Parametro("nome", "windson", 10)
        Assertions.assertEquals(resultado, parametro.toString())
    }

    @Test
    fun tamanhoFull(){
        val resultado = "windson"
        val parametro = Parametro("nome", "windson", 6)
        Assertions.assertEquals(resultado, parametro.toString())
    }

    @Test
    fun menor(){
        val resultado = "windson"
        val parametro = Parametro("nome", "windson", 5)
        Assertions.assertEquals(resultado, parametro.toString())
    }
}