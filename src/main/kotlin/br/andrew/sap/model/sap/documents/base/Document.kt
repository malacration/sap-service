package br.andrew.sap.model.sap.documents.base

import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.WarehouseDefault
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.forca.EnderecoId
import br.andrew.sap.model.sap.ReconciliationRow
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.adiantamento.DownPaymentsToDraw
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    val DocDueDate : String?,
                    val DocumentLines : List<DocumentLines>,
                    private val BPL_IDAssignedToInvoice : String) : ReconciliationRow, BatchId{

    var comments: String? = null
    var docDate :String? = null
    var salesPersonCode: Int = -1
    var paymentGroupCode: Int? = null
    var docEntry : Int? = null
    var docNum : String? = null
    var paymentMethod : String? = null
    var documentInstallments : List<Installment>? = null
    var journalMemo : String? = null
    var Cancelled : Cancelled? = null
    var u_pedido_update : String? = "0"
    var DocTotal : String? = null
    var discountPercent : Double? = null
    var totalDiscount : String? = null
    var SequenceSerial : String? = null
    var sequenceModel : String? = null //->
    var CreateDate : String? = null

    @JsonProperty("U_id_pedido_forca")
    var u_id_pedido_forca: String? = null

    @JsonProperty("U_uuid_forca")
    var u_uuid_forca: String? = null

    var cardName: String? = null
    var OpeningRemarks: String? = null
    var controlAccount: String? = null
    var model : Int? = null
    var docType: String? = null
    var docObjectCode : DocumentTypes? = null

    @JsonProperty("DocumentStatus")
    val DocumentStatus : DocumentStatus? = null
    var documentAdditionalExpenses : MutableList<AdditionalExpenses> = mutableListOf()
    var shipToCode : String? = null
    var Address : String? = null
    var U_assinatura : String = "0"
    var U_rd_station : String? = null
    var U_venda_futura: Int? = null
    var U_entrega_vf: Int = 0
    var downPaymentsToDraw : List<DownPaymentsToDraw>? = null
    var TransNum : Int? = null
    var SequenceCode : Int? = null

    @JsonProperty("TaxExtension")
    var TaxExtension: TaxExtension? = TaxExtension()

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice
    }

    fun productsByTax(): Map<String, List<Product>> {
        return this.DocumentLines
                .filter { it is Product && it.TaxCode != null && it.TaxCode!!.isNotEmpty() }
                .groupBy { it.TaxCode!! } as Map<String, List<Product>>
    }

    fun usaBrenchDefaultWarehouse(branchs : List<WarehouseDefault>){
        branchs.firstOrNull{ it.BPLID == BPL_IDAssignedToInvoice }
                ?.also { usaBrenchDefaultWarehouse(it) }

    }
    fun setDistribuicaoCusto(distCusto : List<DistribuicaoCustoByBranch>){
        distCusto.firstOrNull{it.branch == BPL_IDAssignedToInvoice}?.also { custoByBranch ->
            this.documentAdditionalExpenses.forEach{
                it.setDistribuicaoCusto(custoByBranch)
            }
            this.DocumentLines.forEach{
                it.setDistribuicaoCusto(custoByBranch)
            }
        }
    }

    fun usaBrenchDefaultWarehouse(default : WarehouseDefault){
        if(default.defaultWarehouseID != null)
            DocumentLines
                    .filter { it.WarehouseCode == null}
                    .forEach { it.WarehouseCode = default.defaultWarehouseID}
    }

    @JsonIgnore
    fun isAvista(): Boolean {
        return paymentGroupCode == -1
    }

    @JsonIgnore
    fun isCalculaDesonaerado(): Boolean {
        return u_pedido_update == "1"
    }

    @JsonIgnore
    fun total() : Double {
        return DocumentLines.sumOf { it.total().setScale(2,RoundingMode.HALF_UP) }
            .plus(totalDespesaAdicional())
            .setScale(2,RoundingMode.HALF_UP).toDouble()
    }

    fun totalNegociado() : BigDecimal {
        return DocumentLines.sumOf { it.totalNegociado() }.setScale(2)
    }

    fun totalProdutos() : BigDecimal {
        return DocumentLines.sumOf { it.total() }.setScale(2,RoundingMode.HALF_UP)
    }

    fun totalDespesaAdicional(): BigDecimal {
        return documentAdditionalExpenses.sumOf { BigDecimal(it.LineTotal) }
    }


    fun presumeDesonerado(rate : Double) : Double {
        return DocumentLines.sumOf { it.presumeDesonerado(rate) }
    }

    @JsonIgnore
    override fun transNumReconciliation(): Int {
        return this.TransNum ?: throw Exception("Nao existe numero de transaction")
    }

    override fun getId(): String {
        return this.docEntry.toString()
    }

    override fun toString(): String {
        return "Document(CardCode='$CardCode', Branch='$BPL_IDAssignedToInvoice', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }

    fun setPix(request: RequestPixDueDate, chave: DataRetonroPixQrCode) {
        if(request.docEntry() != docEntry)
            throw Exception("O qrCode nao pertence a esse documento")
        this.documentInstallments!!.find { it.InstallmentId == request.getInstallmentId() }?.also {
            it.U_QrCodePix = chave.data.textContent
            it.U_pix_textContent = chave.data.textContent
            it.U_pix_link = chave.data.link
            it.U_pix_reference = chave.data.reference
        }
    }

    fun getInstallmentBy(transaction: Transaction): Installment? {
        val parcelas = documentInstallments?.filter { it.getBy(transaction) } ?: listOf()
        if(parcelas.size > 1)
            throw Exception("Existe mais de uma parcela para baixar")
        return parcelas.firstOrNull()
    }

    fun aplicaDescontoDesonerado() {
        this.totalDiscount = null
        this.discountPercent = null
        this.DocTotal = null
        this.Address = null
        val desonerado = DocumentLines.sumOf { it.valorDesonerado + it.resto }.setScale(4,RoundingMode.HALF_UP)
        val totalAntesDesconto = BigDecimal(total()).setScale(2,RoundingMode.HALF_UP)
        this.discountPercent =
            if(totalAntesDesconto.compareTo(BigDecimal.ZERO) == 0 )
                0.0
            else
                desonerado
                    .divide(totalAntesDesconto, 6,RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100)).toDouble()
    }

    fun associaEndereco(endereco: EnderecoId){
        this.shipToCode = endereco.code
    }

    fun atualizaPrecoBase(itemService: ItemsService) {
        DocumentLines.forEach{it.atualizaPrecoBase(itemService)}
    }

    var frete : Double? = null
        set(value) {
            if(value != null)
                this.documentAdditionalExpenses.add(AdditionalExpenses.frete(value))
            field = null
        }

    fun calcularDataDeVencimento(minimoDias : Long = 5): LocalDateTime {
        return calcularDataDeVencimento(
            this.DocDueDate?: throw Exception("Documento sem data de vencimento")
            ,minimoDias)
    }
    fun calcularDataDeVencimento(dataEntradaString: String, minimoDias : Long = 5): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dataEntrada = LocalDateTime.parse(dataEntradaString, formatter)
        val dataMinimaDeVencimento = LocalDateTime.now(ZoneOffset.UTC).plusDays(minimoDias)
        return listOf(dataEntrada, dataMinimaDeVencimento)
            .maxBy{ it }
    }

    @JsonIgnore
    fun reverseDocumentLine(): List<DocumentLines> {
        return this.DocumentLines.map {
            val pt = if(it.ItemCode != null)
                Product(it.ItemCode!!,it.Quantity,it.UnitPrice,it.Usage)
            else
                Service(it.UnitPrice,it.Quantity)
            pt.BaseLine = it.LineNum
            pt.BaseEntry = this.docEntry
            pt.BaseType = this.docObjectCode?.value ?: throw Exception("Sem object type")
            pt
        }

    }

}

