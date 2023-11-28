package br.andrew.sap.integration

import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.documents.DocumentReport
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.documents.base.Product
import br.andrew.sap.model.partner.BPFiscalTaxID
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.CpfCnpj
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@SpringBootTest
class Pedido4Assinatura {

    @Autowired
    var template : TemplateEngine? = null
    @Test
    fun teste() {
        val paymentMethod = PaymentMethod("01").also {
            it.Description = "PAGAMENTO BOLETOSB.BRASIL CC 1199"
        }
        val businessPartner = BusinessPartner().also {
            it.cardName = "Windson Filipe Garça"
            it.emailAddress = "email@mail.com"
            it.setBPFiscalTaxIDCollection(listOf(BPFiscalTaxID(CpfCnpj("12345678901"),"")))
        }
        val businesplace = BussinessPlace().also {
            it.BPLName = "Empresa teste"
        }
        val itens = listOf(
            Product("Code-1","5","10.20").also{
                it.MeasureUnit = "Saco 30KG"
                it.ItemDescription = "Produto 1"
            }
        )
        val document = Document("1","", itens,"").also {
            it.docNum = "1"
            it.DocTotal = "51.00"
            it.Address = "LINHA 27RIO DAS GARÇAS,LOTE 113 A 116\n" +
                    "CHACARA BOA ESPERANÇA\n" +
                    "76800000--RO\n" +
                    "BRASIL"
        }
        val teste = DocumentReport(document, businessPartner, businesplace,paymentMethod)
        val saida = template!!.process("pedido-4-assinatura",
            Context().also {
                it.setVariable("p", teste)
            })
        assert(saida.contains("Produto 1"))
    }
}