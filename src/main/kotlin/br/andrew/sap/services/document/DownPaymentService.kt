package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.bank.PaymentInvoice
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.model.payment.HandlePaymentTermsLines
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.BoletoVf
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import JournalEntry
import JournalEntryLines
import br.andrew.sap.schedules.futura.Soma
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.invent.OrigemBoletoEnum
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@Service
class DownPaymentService(env: SapEnvrioment,
                         val sqlQueriesService: SqlQueriesService,
                         private val orderService: OrdersService,
                         private val paymentService : PaymentTermsTypesService,
                         private val bankplus : BankPlusService,
                         private val accountsReceivableService: AccountsReceivableService,
                         private val incomingPaymentService: IncomingPaymentService,
                         private val creditNotesService: CreditNotesService,
                         private val batchService: BatchService,
                         private val journalEntriesService: JournalEntriesService,
                         private val pixService: DynamicPixQrCodeService,
                         private val businessPartnersService: BusinessPartnersService,
                         @Value("\${venda-futura.adiantamento-item:none}") val vfItemAdiantamento : String,
                         @Value("\${venda-futura.adiantamento-utilizacao:66}") val vfUtilizacao : Int,
                         @Value("\${adiantamento-vf.formaPagamento:none}") vfFormaPagamento : String,
                         restTemplate: RestTemplate,
                         authService: AuthService,
) : EntitiesService<Document>(env, restTemplate, authService) {

    val vfFormaPagamento: String? = if(vfFormaPagamento == "none")  null else vfFormaPagamento

    val logger: Logger = LoggerFactory.getLogger(DownPaymentService::class.java)

    override fun path(): String {
        return "/b1s/v1/DownPayments"
    }

    fun adiantamentosVendaFuturaSave(contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        val adiantamento = adiantamentosVendaFuturaWithoutSave(contrato,paymentInfo)
        adiantamento.journalMemo = "Fatura de adiantamento venda futura. Contrato ${contrato.DocEntry}"
        adiantamento.comments = adiantamento.journalMemo
        return save(adiantamento).tryGetValue<Document>()
    }

    /**
     * Monta a devolução (CreditNotes) de um adiantamento de venda futura.
     * Quando a linha do adiantamento original não possui utilização, usa a
     * utilização configurada em [vfUtilizacao] ([venda-futura.adiantamento-utilizacao]).
     */
    fun devolucaoAdiantamentoVendaFutura(adiantamento: Document): CreditNotes {
        return CreditNotes(adiantamento).also { aplicaUtilizacaoDevolucao(it) }
    }

    private fun aplicaUtilizacaoDevolucao(devolucao: CreditNotes) {
        devolucao.DocumentLines.forEach { linha ->
            if (linha.Usage == null)
                linha.Usage = vfUtilizacao
        }
    }

    fun adiantamentosVendaFuturaWithoutSave(contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        if(vfItemAdiantamento == "none")
            throw Exception("O parametro [venda-futura.adiantamento-item] nao pode ser $vfItemAdiantamento")
        val linhas = listOf(Product(vfItemAdiantamento,paymentInfo.value.toString(),"1",vfUtilizacao))
        val adiantamento = DownPayment(
            contrato.U_cardCode,
            paymentInfo.dueDate.toString(),
            linhas,
            contrato.U_filial.toString())
        if(vfFormaPagamento != null)
            adiantamento.paymentMethod = vfFormaPagamento
        adiantamento.U_venda_futura = contrato.DocEntry;
        adiantamento.salesPersonCode = contrato.U_vendedor
        return adiantamento
    }

    fun getLastInstallment(contrato: Contrato): String {
        val boleto = getByContratoVendaFutura(
            contrato.DocEntry?: throw Exception("O id do contrato nao pode ser nullo"),
            OrderBy("DocDueDate",Order.DESC)
        ).firstOrNull() ?: throw Exception("Contrato invalido pois nao tem nenhum boleto")
        return boleto.DocDueDate ?: throw Exception("O ultimo boleto nao tem data de vencimento")
    }

    fun getByContratoVendaFutura(id: Int, orderBy : OrderBy = OrderBy()): List<Document> {
        val filter = Filter(
            Predicate("U_venda_futura",id,Condicao.EQUAL)
        )
        return get(filter,orderBy).tryGetValues()
    }

    fun getByContratoVendaFuturaStatus(id: Int): List<BoletoVf> {
        val parametros = listOf(Parameter("idVendaFutura",id),)
        val statusSql = sqlQueriesService.execute("boletos-status.sql",parametros)
            ?.tryGetValues<BoletoVf>()
            ?: listOf()
        val devolucaoPorDocNum = statusSql
            .filter { !it.DocNum.isNullOrBlank() }
            .associate { it.DocNum to it.devolucao }

        return getByContratoVendaFutura(id, OrderBy("DocDueDate", Order.ASC))
            .flatMap { document ->
                val downPayment = document.docEntry
                    ?.let { getById(it).tryGetValue<DownPayment>() }
                    ?: document
                val devolucao = devolucaoPorDocNum[downPayment.docNum]
                val installments = downPayment.documentInstallments.orEmpty()
                if(installments.isEmpty()) {
                    listOf(BoletoVf.from(downPayment, devolucao = devolucao))
                } else {
                    installments.map { BoletoVf.from(downPayment, it, devolucao) }
                }
            }
    }

    fun getOurNumbersByContratoVendaFutura(id: Int): List<String> {
        return getByContratoVendaFutura(id)
            .flatMap { adiantamento ->
                val docEntry = adiantamento.docEntry ?: return@flatMap listOf()
                try {
                    bankplus.getBoletosBy(
                        adiantamento.getBPL_IDAssignedToInvoice(),
                        docEntry.toString(),
                        OrigemBoletoEnum.adiantamento
                    )
                } catch (e: Exception) {
                    logger.warn(
                        "Nao foi possivel buscar numero do boleto no BankPlus. contrato={}, adiantamento={}",
                        id,
                        docEntry,
                        e
                    )
                    listOf()
                }
            }
            .mapNotNull { it.nossoNumero }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun createPixByContratoVendaFutura(id: Int): List<BoletoVf> {
        getByContratoVendaFutura(id)
            .filter {
                it.DocumentStatus == DocumentStatus.bost_Open &&
                    it.Cancelled != br.andrew.sap.model.enums.Cancelled.tYES
            }
            .forEach { document ->
                val docEntry = document.docEntry
                    ?: throw Exception("Adiantamento do contrato $id sem DocEntry")
                val downPayment = getById(docEntry).tryGetValue<DownPayment>()
                createPix(downPayment)
            }
        return getByContratoVendaFuturaStatus(id)
    }

    fun createPix(
        downPayment: DownPayment,
        parcelas: List<Int> = listOf(),
        jurosMoraPercent: Double = 0.0
    ): List<Installment> {
        if(downPayment.documentInstallments.isNullOrEmpty()) {
            throw Exception(
                "O adiantamento ${downPayment.docEntry} do contrato ${downPayment.U_venda_futura} " +
                    "nao possui parcelas no SAP"
            )
        }
        val partner = businessPartnersService
            .getById("'${downPayment.CardCode}'")
            .tryGetValue<BusinessPartner>()
        val builder = RequestPixDueDateSemContaBuilder(
            partner,
            downPayment,
            parcelas,
            jurosMoraPercent
        )
        val requests = builder.build()
        val parcelasSolicitadas = builder.parcelasSolicitadas()
        if(requests.isEmpty()) {
            return parcelasSolicitadas
        }
        val installmentsAtualizadas = requests.mapNotNull {
            downPayment.setPix(it, pixService.genereateFor(it))
        }
        updatePixInstallments(
            downPayment.docEntry ?: throw Exception("DocEntry do adiantamento nao pode ser nulo"),
            installmentsAtualizadas
        )
        return parcelasSolicitadas
    }

    fun updatePixInstallments(docEntry: Int, installments: List<Installment>) {
        PixInstallmentUpdater.atualizar(
            this, sqlQueriesService, "down-payment-installment-pix-persistido.sql",
            docEntry, installments, "do adiantamento"
        )
    }

    fun getPixsGeradosParaConsulta(dataReferencia: String): List<InstallmentPixConsulta> {
        return sqlQueriesService.getAll(
            "down-payment-installment-pix-consulta.sql",
            listOf(Parameter("now", dataReferencia))
        )
    }

    fun getPixsVencidos(dataReferencia: String): List<InstallmentPixConsulta> {
        return sqlQueriesService.getAll(
            "down-payment-installment-pix-expirado.sql",
            listOf(Parameter("now", dataReferencia))
        )
    }

    fun getDownPaymentByIdPix(reference: String): DownPayment {
        val list = sqlQueriesService.execute(
            "down-payment-by-pix-reference.sql",
            listOf(Parameter("reference", reference))
        )?.tryGetValues<DocEntry>()
            ?.mapNotNull { getBy(it).tryGetValue<DownPayment>() } ?: listOf()
        if(list.size > 1) throw Exception("Mais de um adiantamento encontrado para o pix ${reference}")
        return list.firstOrNull() ?: throw Exception("Nenhum adiantamento encontrado para o pix ${reference}")
    }

    fun baixaPixBy(downPayment: DownPayment, installment: Installment, transaction: Transaction, conta: ContaUzziPayPix): Transaction {
        return accountsReceivableService.baixaPixBy(downPayment, installment, transaction, conta)
    }

    fun estornarPorDevolucao(
        downPayment: DownPayment,
        motivo: String = "Estorno automatico de adiantamento PIX expirado."
    ): CreditNotes {
        val docEntry = downPayment.docEntry ?: throw Exception("DocEntry do adiantamento nao pode ser nulo")
        val devolucaoExistente = sqlQueriesService
            .execute("devolucao-adiantamento.sql", Parameter("docEntry", docEntry))
            ?.tryGetValues<DocEntry>()
            ?.firstOrNull()

        if (devolucaoExistente?.DocEntry != null) {
            return creditNotesService.getById(devolucaoExistente.DocEntry!!).tryGetValue<CreditNotes>()
        }

        val devolucao = CreditNotes(downPayment).also {
            val referencia = downPayment.docNum ?: docEntry.toString()
            it.comments = "$motivo Adiantamento $referencia"
            it.journalMemo = it.comments
            it.U_TX_DocEntryRef = docEntry
            it.U_TX_DocTypeRef = downPayment.docObjectCode?.value
            it.SequenceCode = 1
            aplicaUtilizacaoDevolucao(it)
        }
        return creditNotesService.save(devolucao).tryGetValue<CreditNotes>()
    }

    fun adiantamentosAbertos(invoice : Invoice): List<DownPayment> {
        val exp = Exception("Nao foi possivel buscar o adianemtno, entrega de venda sem id contrato")
        return adiantamentosAbertos(invoice.CardCode,invoice.U_venda_futura?: throw exp)
    }

    fun adiantamentosAbertos(cardCode : String, vendaFutura : Int): List<DownPayment> {
        val adiantamentoFilter = Filter(
            Predicate("U_venda_futura",vendaFutura,Condicao.EQUAL),
            Predicate("CardCode",cardCode,Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Close,Condicao.EQUAL),
            //Talvez DownPaymentStatus indique se tem saldo a apropriar ou nao
        )
        return get(adiantamentoFilter).tryGetValues<DownPayment>()
            .filter {
                (sqlQueriesService
                    .execute("devolucao-adiantamento.sql", Parameter("docEntry", it.docEntry!!))
                    ?.tryGetValues<DocEntry>() ?: listOf()).isEmpty()
            }
            .map{
                val apropriacoes : List<Soma> = sqlQueriesService
                    .execute("adiantamento-apropriado.sql", Parameter("docEntry",it.docEntry!!))
                    ?.tryGetValues<Soma>() ?: listOf(Soma(BigDecimal.ZERO))
                if(apropriacoes.size > 1)
                    throw Exception("Nao pode ter mais de uma apropriacao para um adiantamento")
                it.apropriado = apropriacoes.firstOrNull()?.soma ?: BigDecimal.ZERO
                it
        }
    }

    fun createAdiantamentoBycontrato(contrato : Contrato, carenciaDias : Int){
        val boletos = this.getByContratoVendaFutura(contrato.DocEntry?.toInt() ?: throw Exception("Contrato sem DocEntry"))
        if(boletos.size > 0)
            throw Exception("Não é possivel emitir boletos para um contrato que ja tem boletos")

        val order = try {
            orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        } catch (e: Exception) {
            throw Exception("O pedido do contrato nao foi encontrado")
        }
        val hanndlePaymentTerms = HandlePaymentTermsLines(
            paymentService.getParcelas(order.paymentGroupCode?: throw Exception("Erro ao pegar da condição de pagamento"))
        )
        hanndlePaymentTerms.calculaVencimentos(contrato,carenciaDias).map {
            val adiantamento = this.adiantamentosVendaFuturaSave(contrato,it)
            try{
                bankplus.geraBoletos(
                    adiantamento.getBPL_IDAssignedToInvoice().toInt(),
                    adiantamento.docEntry ?: throw Exception("Falha ao obter docentry do adiantamento"),
                    0,
                    OrigemBoletoEnum.adiantamento)
            }catch (e : Exception){
                logger.error("Erro ao gerar boleto",e)
            }
        }
    }
}
