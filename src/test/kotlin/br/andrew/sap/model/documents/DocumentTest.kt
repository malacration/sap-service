package br.andrew.sap.model.documents

import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.forca.Produto
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.services.ComissaoServiceMock
import br.andrew.sap.services.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class DocumentTest{

    @Test
    fun testeTotalToBigDecimal(){
        val produtos = listOf(Product("windson","100","1.7732",10).also { it.U_preco_negociado = 1.7732 })
        val document = Invoice("","",produtos,"2")
        Assertions.assertEquals("177.32",document.totalNegociado().toString())
    }
}