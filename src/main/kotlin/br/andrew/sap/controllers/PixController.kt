package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.PixDocType
import br.andrew.sap.model.sap.documents.matches
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("pix")
class PixController(
    val transactionsPixService: TransactionsPixService,
    val uzziPayEnvrioment: UzziPayEnvrioment,
    val bussinessPlaceService : BussinessPlaceService,
    val bussinesPartnersService : BusinessPartnersService,
    val invoiceService: InvoiceService){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }

    @GetMapping("gerar-chave/{pixDocType}/{docNum}")
    fun gerarChave(@PathVariable pixDocType : PixDocType, @PathVariable docNum : Int): List<RequestPixDueDate> {

        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService
                .get(Filter("DocNum",docNum, Condicao.EQUAL))
                .tryGetValues<Invoice>()
            val bussinessPlace = bussinessPlaceService
                .getById(invoice.first().getBPL_IDAssignedToInvoice())
                .tryGetValue<BussinessPlace>()
            val partner = bussinesPartnersService.getById("'${invoice.first().CardCode}'").tryGetValue<BusinessPartner>()

            invoiceService.createPix(invoice.firstOrNull() ?: throw Exception("Documento não foi encontrado"))
            pixService.genereateFor(it)
            return RequestPixDueDateSemContaBuilder(partner,bussinessPlace,invoice.first()).build()
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
        return listOf()
//        return transactionsPixService.getBy(id)
    }

    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Transaction {
        return transactionsPixService.getBy(id)
    }

    @GetMapping("transaction/{id}/conta/{cnpj}/baixa")
    fun verificaPixEhBaixa(@PathVariable id : String, @PathVariable cnpj : String): Any {
        val conta : ContaUzziPayPix = uzziPayEnvrioment.getContaByCnpj(cnpj)
        val transaction = transactionsPixService.getBy(id,conta)
        return invoiceService.baixaPixBy(transaction,conta)
    }
}
