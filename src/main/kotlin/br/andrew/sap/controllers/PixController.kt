package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.PixRequestAdiantamento
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.dto.PixGeradoResponse
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.PixDocType
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.matches
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
@RequestMapping("pix")
class PixController(
    val transactionsPixService: TransactionsPixService,
    val invoiceService: InvoiceService,
    val pixPaymentVerificationService: PixPaymentVerificationService,
    val pixDynamicService : DynamicPixQrCodeService,
    val adiantamentoService : DownPaymentService,
    @Value("\${pix.adiantamento-item:none}") val pixItemAdiantamento : String,
    @Value("\${pix.juros.mora.percent:0}") val jurosMoraPercent: Double){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }

    @PostMapping("gerar-chave")
    fun gerarChavePixByValor(
        @RequestBody pixRequest : PixRequestAdiantamento,
        auth : Authentication
    ): PixGeradoResponse {
        val adiantamentoOld = if(pixRequest.docEntry != null && pixRequest.documentTypes == DocumentTypes.oOrders){
            val filterAdiantamentoOld = Filter(
                Predicate("U_TX_DocEntryRef",pixRequest.docEntry, Condicao.EQUAL),
                Predicate("DocumentStatus",DocumentStatus.bost_Open, Condicao.EQUAL),
            )
            adiantamentoService.get(filterAdiantamentoOld)
                .tryGetValues<DownPayment>()
                .firstOrNull()
        }else null

        val installmentOld = adiantamentoOld?.documentInstallments?.firstOrNull()
        if (adiantamentoOld != null && installmentOld != null && installmentOld.isPixValido()) {
            installmentOld.DocEntry = adiantamentoOld.docEntry
            return PixGeradoResponse(installmentOld, 0.0)
        }

        if (adiantamentoOld != null) {
            return atualizarPixAdiantamento(adiantamentoOld, pixRequest)
        }

        if(pixItemAdiantamento == "none")
            throw Exception("Não foi configurado um item de pix para o adiantamento")
        val linhas = listOf(Product(pixItemAdiantamento,"1",pixRequest.valor.toString()))
        val adiantamento = DownPayment(
            pixRequest.cardCode,
            LocalDate.now().toString(),
            linhas,
            pixRequest.idFilial.toString())
        adiantamento.U_TX_DocEntryRef = pixRequest.docEntry
        adiantamento.U_TX_DocTypeRef = pixRequest.documentTypes?.value

        val adiantamentoSalvo = adiantamentoService.save(adiantamento).tryGetValue<DownPayment>()
        val adiantamentoComParcelas = adiantamentoService.getById(
            adiantamentoSalvo.docEntry ?: throw Exception("Falha ao obter docEntry do adiantamento gerado")
        ).tryGetValue<DownPayment>()
        return atualizarPixAdiantamento(adiantamentoComParcelas, pixRequest)
    }

    private fun atualizarPixAdiantamento(
        adiantamento: DownPayment,
        pixRequest: PixRequestAdiantamento
    ): PixGeradoResponse {
        val immediate = pixRequest.getImmediateRequest(adiantamento)
        val dadosPix = pixDynamicService.genereateFor(immediate)
        val installmentAtualizado = adiantamento.setPix(immediate, dadosPix)
            ?: throw Exception("Parcela do adiantamento não encontrada para associar o pix")
        adiantamentoService.updatePixInstallments(
            adiantamento.docEntry ?: throw Exception("Falha ao obter docEntry do adiantamento"),
            listOf(installmentAtualizado)
        )
        installmentAtualizado.DocEntry = adiantamento.docEntry
        return PixGeradoResponse(installmentAtualizado, 0.0)
    }

    @GetMapping("gerar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun gerarChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int,
        @RequestParam("juros", defaultValue = "true") juros: Boolean = true,
        auth : Authentication
    ): List<PixGeradoResponse> {
        if(auth !is User)
            throw Exception("Não foi possivel fazer a conversão de auth para User")
        if(!auth.isAllCreatePix(juros))
            throw Exception("Não é permitido criar pix!")
        val jurosPercent = if(juros) jurosMoraPercent else 0.0
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()
            return invoiceService.createPix(invoice,parcela,jurosPercent).map { installment ->
                PixGeradoResponse(installment, jurosPercent)
            }
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("checar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun checkChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int,
    ): Transaction {
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry)
                .tryGetValue<Invoice>()
            val parcelaPix = invoice.documentInstallments?.firstOrNull { it.InstallmentId == parcela }
            return pixPaymentVerificationService.verificaPixEhBaixa(
                invoice,
                parcelaPix ?: throw Exception("Referencia a Parcela não encontrada")
            )
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Transaction {
        return transactionsPixService.getBy(id)
    }

    @GetMapping("transaction/{id}/conta/{cnpj}/baixa")
    fun verificaPixEhBaixa(@PathVariable id : String, @PathVariable cnpj : String): Transaction {
        //TODO mudar isso depois.
        throw Exception("Erro, esse parametro agora utiliza ID da filial e nao cnpj")
    }
}
