package br.andrew.sap.model.sap

import JournalEntryLines
import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

class BatchesGroupByItemCodeTesst {


    @Test
    fun temQueFalharItensDiferentes(){
        val batch = BatchesGroupByItemCode().also {
            it.ItemCode = "WINDSON"
            it.Batches = listOf<BatchStock>(BatchStock("123","2","WINDSON","deposito"))
        }
        val line = Product("roberto","1","1")
        assertThrows<Exception> {
            batch.getBachesBy(line)
        }
    }

    @Test
    fun temQueFalharDepositoDiferente(){
        val batch = BatchesGroupByItemCode().also {
            it.ItemCode = "WINDSON"
            it.Batches = listOf<BatchStock>(BatchStock("123","2","WINDSON","deposito"))
        }
        val line = Product("roberto","1","1").also {
            it.WarehouseCode = "deposito-diferente"
        }
        assertThrows<Exception> {
            batch.getBachesBy(line)
        }
    }



    @Test
    fun pegarUmLote(){
        val batch = BatchesGroupByItemCode().also {
            it.ItemCode = "WINDSON"
            it.Batches = listOf<BatchStock>(BatchStock("123","2","WINDSON","deposito"))
        }
        val line = Product("WINDSON","1","1").also {
            it.WarehouseCode = "deposito"
        }
        val resultado = batch.getBachesBy(line)
        assertEquals(1,resultado.size)
        assertEquals("1",resultado.first().Quantity)
        assertEquals("123",resultado.first().BatchNumber)
    }

    @Test
    fun pegaDoisLotes(){
        val batch = BatchesGroupByItemCode().also {
            it.ItemCode = "WINDSON"
            it.Batches = listOf<BatchStock>(
                BatchStock("123","1","WINDSON","deposito"),
                BatchStock("321","1","WINDSON","deposito"),
                BatchStock("666","1","WINDSON","deposito")
            )
        }
        val line = Product("WINDSON","2","1").also {
            it.WarehouseCode = "deposito"
        }
        val resultado = batch.getBachesBy(line)
        assertEquals(2,resultado.size)
        assertEquals("1",resultado.first().Quantity)
        assertEquals("123",resultado.first().BatchNumber)
        assertEquals("1",resultado.get(1).Quantity)
        assertEquals("321",resultado.get(1).BatchNumber)
    }

    @Test
    fun testaComDoisPedidos(){
        val batch = BatchesGroupByItemCode().also {
            it.ItemCode = "WINDSON"
            it.Batches = listOf<BatchStock>(
                BatchStock("123","2","WINDSON","deposito"),
            )
        }
        val pedido1 = Product("WINDSON","1","1").also {
            it.WarehouseCode = "deposito"
        }
        val pedido2 = Product("WINDSON","1","1").also {
            it.WarehouseCode = "deposito"
        }
        val resultado1 = batch.getBachesBy(pedido1)
        assertEquals(1,resultado1.size)
        assertEquals("1",resultado1.first().Quantity)
        assertEquals("123",resultado1.first().BatchNumber)

        val resultado2 = batch.getBachesBy(pedido2)
        assertEquals(1,resultado2.size)
        assertEquals("1",resultado2.first().Quantity)
        assertEquals("123",resultado2.first().BatchNumber)

    }

}