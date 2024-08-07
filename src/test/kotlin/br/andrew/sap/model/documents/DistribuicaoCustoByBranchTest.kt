package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DistribuicaoCustoByBranchTest{
    @Test
    fun teste(){
        val product = Product("teste", "2","2.90",9)
        product.setDistribuicaoCusto(DistribuicaoCustoByBranch("2","500","50000201"))
        assertEquals("500",product.CostingCode)
    }
}