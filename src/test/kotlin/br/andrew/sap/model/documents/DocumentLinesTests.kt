package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DocumentLinesTests {

    @Test
    fun toStringTest(){
        val doc = Product("ItemCode","30","20")
        doc.ItemDescription = "WindsonProduct"
        Assertions.assertEquals("ItemCode - WindsonProduct - 30",doc.toString())
    }

    @Test
    fun toStringList(){
        val doc = Product("ItemCode","30","20")
        doc.ItemDescription = "WindsonProduct"
        Assertions.assertEquals("ItemCode - WindsonProduct - 30\nItemCode - WindsonProduct - 30",listOf(doc,doc).joinToString("\n"))
    }
}