package br.andrew.sap.model

import br.andrew.sap.model.sap.InternalReconciliationOpenTransRow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InternalReconciliationOpenTransRowTests {

    @Test
    fun negativo(){
        val negativo = InternalReconciliationOpenTransRow().also {
            it.reconcileAmount = -10.0
        }
        Assertions.assertEquals(10.0,negativo.reconcileAmount)
    }

    @Test
    fun positivo1(){
        val negativo = InternalReconciliationOpenTransRow().also {
            it.reconcileAmount = 10.0
        }
        Assertions.assertEquals(10.0,negativo.reconcileAmount)
    }
}