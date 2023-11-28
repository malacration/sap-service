package br.andrew.sap.schedules

import br.andrew.sap.controllers.Pedido4AssinaturaController
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.User
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.documents.DocumentReport
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.PdfService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.bank.WizardPaymentMethodService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.document.QuotationsService
import br.andrew.sap.services.softexpert.DocumentExpertService
import br.andrew.sap.services.softexpert.WorkFlowService
import br.andrew.sap.workflow.NewAssocDocumentResponseType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context


@Component

class CreateSignatureTaskSe(
    val service : QuotationsService,
    val telegramRequestService: TelegramRequestService,
    val workflowService: WorkFlowService,
    val controller: Pedido4AssinaturaController,
    val documentExpertService: DocumentExpertService) {

    val logger: Logger = LoggerFactory.getLogger(CreateSignatureTaskSe::class.java)


    @Scheduled(fixedDelay = 30000)
    fun execute() {
        try {
            val predicados = listOf(
                Predicate("U_assinatura", "1", Condicao.EQUAL)
            )
            var requests: OData? = null
                do {
                    requests = if (requests == null)
                        service.get(Filter(predicados))
                    else
                        service.next(requests)
                    requests.tryGetValues<Document>()
                        .forEach {
                            try {
                                service.update(
                                    "{ \"U_assinatura\" : \"0\" }",
                                    it.docEntry.toString()
                                )
                                executa(it)
                            } catch (t: Throwable) {
                                telegramRequestService.send("Erro ai criar assinatura para o pedido ${it.docEntry}. ${t.message}")
                                logger.error("Erro ai criar assinatura para o pedido ${it.docEntry}",t)
                            }
                        }
                } while (requests!!.hasNext())
        } catch (t : Throwable){
            logger.error(t.message, t)
        }
    }

    fun executa(order : Document): NewAssocDocumentResponseType {
        val report = controller.report(order)
        val pdf = controller.pdf(report)
        val titulo = "Pedido Venda ${order.docNum} para assinatura"
        val taskInstance = workflowService.criaFluxoAssinaatura("assinatura-pedido",report.titule, report.getDate())
        val pdfAttachable = documentExpertService.save(titulo,titulo,
            "PEDIDO-VENDA2",pdf)
        return workflowService.associa("dados",taskInstance,pdfAttachable)
    }

}