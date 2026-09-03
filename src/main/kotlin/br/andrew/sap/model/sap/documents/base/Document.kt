package br.andrew.sap.model.sap.documents.base

import br.andrew.sap.model.enums.CancelStatus
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sistema.WarehouseDefault
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.forca.EnderecoId
import br.andrew.sap.model.sap.comercial.DebOrCredt
import br.andrew.sap.model.sap.comercial.ReconciliationListRows
import br.andrew.sap.model.sap.comercial.ReconciliationRow
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.adiantamento.DownPaymentsToDraw
import br.andrew.sap.model.self.vendafutura.ContratoParse.Companion.parse
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.RequestPixImmediate
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.stock.ItemsService
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
import kotlin.collections.mapIndexed

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    var DocDueDate : String?,
                    val DocumentLines : List<DocumentLines>,
                    private val BPL_IDAssignedToInvoice : String) : ReconciliationListRows, BatchId{

    var comments: String? = null
    var docDate :String? = null
    var salesPersonCode: Int = -1
    var salesEmployeeName: String? = null
    var paymentGroupCode: Int? = null
    var docEntry : Int? = null
    var docNum : String? = null
    var paymentMethod : String? = null
    var documentInstallments : List<Installment>? = null
    var journalMemo : String? = null
    var Cancelled : Cancelled? = null

    //somente leitura: o Jackson le o que o SAP responde no GET (@set:JsonProperty) mas
    //NUNCA manda de volta (@get:JsonIgnore) - CancelStatus nao e campo gravavel no Service
    //Layer, e manda-lo num POST/PATCH derruba a chamada com "Internal error (-5002)", igual
    //aconteceu com LineTaxJurisdictions/OrCreateTaxExtension.
    //Os use-site targets (@get:/@set:) sao obrigatorios: sem eles a anotacao vai parar no
    //backing field, que e privado e o Jackson nem enxerga - ficaria sem efeito nenhum.
    @get:JsonIgnore
    @set:JsonProperty("CancelStatus")
    var CancelStatus : CancelStatus? = null
    var u_pedido_update : String? = "0"
    var DocTotal : String? = null
    var discountPercent : Double? = null
    var totalDiscount : String? = null
    var ReserveInvoice : YesNo? = null
    var SequenceSerial : String? = null
    var sequenceModel : String? = null //->
    var CreateDate : String? = null
    var SeriesString : String? = null
    var U_ChaveAcesso : String? = null
    var DflWhs: String? = null
    var U_faturadoOrdemCarregamento : Int = 0

    @JsonProperty("U_id_pedido_forca")
    var u_id_pedido_forca: String? = null

    @JsonProperty("U_uuid_forca")
    var u_uuid_forca: String? = null

    @JsonProperty("U_offline_id")
    var u_offline_id: String? = null

    @JsonProperty("U_offline_user")
    var u_offline_user: String? = null

    var cardName: String? = null
    var OpeningRemarks: String? = null
    var controlAccount: String? = null
    var model : Int? = null
    var docObjectCode : DocumentTypes? = null
    var AttachmentEntry : Int? = null

    @JsonProperty("DocumentStatus")
    var DocumentStatus : DocumentStatus? = null
    var documentAdditionalExpenses : MutableList<AdditionalExpenses> = mutableListOf()
    var AddressExtension : AddressExtension? = null
    var shipToCode : String? = null
    var Address : String? = null
    var Address2 : String? = null
    var U_assinatura : String = "0"
    var U_rd_station : String? = null
    var U_venda_futura: Int? = null
    var U_entrega_vf: Int = 0
    var U_vf_estornada: Int = 0
    var U_legado_vf : String = "0"
    var downPaymentsToDraw : List<DownPaymentsToDraw>? = null
    var TransNum : Int? = null
    var SequenceCode : Int? = null
    var U_TX_DocEntryRef : Int? = null
    var U_TX_DocTypeRef : Int? = null
    var ClosingRemarks: String? = null


    //@JsonIgnore obrigatorio: sem ele o Jackson enxerga isso como getter da propriedade
    //"OrCreateTaxExtension" e manda esse campo inexistente em todo POST/PATCH de documento -
    //o Service Layer responde "Internal error (-5002)" quando recebe propriedade que nao e do SAP.
    @JsonIgnore
    fun getOrCreateTaxExtension(): TaxExtension {
        if (this.TaxExtension == null) {
            this.TaxExtension = TaxExtension()
        }
        return this.TaxExtension!!
    }

    @JsonProperty("TaxExtension")
    var TaxExtension: TaxExtension? = null

    var VehicleState: String? = null
        set(value) {
            field = value
            if (this.TaxExtension == null) {
                this.TaxExtension = TaxExtension()
            }
            this.TaxExtension?.VehicleState = value
        }

    var Incoterms: Int? = null
        set(value) {
            field = value
            if (this.TaxExtension == null) {
                this.TaxExtension = TaxExtension()
            }
            this.TaxExtension?.Incoterms = value
        }

    /**
     * O Incoterms que o documento realmente carrega.
     *
     * O campo de topo so e preenchido por quem monta o Document em Kotlin: o setter escreve
     * PARA DENTRO do TaxExtension, mas nada le de volta. O front manda o valor aninhado
     * ("TaxExtension": {"Incoterms": 0}), entao na desserializacao o campo de topo fica null -
     * foi assim que a validacao de frete ficou sem disparar em nenhuma venda pelo portal.
     *
     * @JsonIgnore obrigatorio: Incoterms nao e campo de topo no SAP, e um getter comum faria o
     * Jackson serializar "Incoterms" na raiz do PATCH. Foi exatamente assim que
     * LineTaxJurisdictions e OrCreateTaxExtension derrubaram o Service Layer com -5002.
     */
    @JsonIgnore
    fun incotermsEfetivo(): Int? {
        return Incoterms ?: TaxExtension?.Incoterms
    }

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice
    }

    fun productsByTax(): Map<String, List<Product>> {
        return this.DocumentLines
                .filter { it is Product && it.TaxCode != null && it.TaxCode!!.isNotEmpty() }
                .groupBy { it.TaxCode!! } as Map<String, List<Product>>
    }

    // Recebe os ids (JurisdictionType) dos impostos desonerados e preenche, em cada linha,
    // o campo lineTotalDesonerado (valor liquido) usando o TaxAmount ja gravado pelo SAP.
    // So e chamado no fluxo de /entregas - fora dele o campo fica null e nao vai ao SAP.
    fun preencheDesonerado(desoneradoIds: List<Int>) {
        DocumentLines.forEach { it.lineTotalDesonerado = it.calcularLineTotalDesonerado(desoneradoIds) }
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

    //Frete do documento (despesa adicional codigo 1), usado para apurar quanto de frete um
    //contrato de venda futura ja cobrou ate agora. Vale o frete negociado quando existe: com
    //ICMS desonerado o LineTotal fica majorado e nao representa o valor combinado.
    @JsonIgnore
    fun freteDespesaAdicional(): BigDecimal {
        return documentAdditionalExpenses
            .filter { it.expenseCode == AdditionalExpenses.CODIGO_FRETE }
            .fold(BigDecimal.ZERO) { acc, it -> acc.plus(BigDecimal(it.valorConferencia().toString())) }
            .setScale(2, RoundingMode.HALF_UP)
    }

    //Base de produtos do documento ja gravado, na mesma definicao que a
    //SBO_SP_VALIDACAO_VENDA_FUTURA usa: DocTotal menos as despesas adicionais.
    @JsonIgnore
    fun baseProdutosFaturada(): BigDecimal {
        return BigDecimal(DocTotal ?: "0")
            .minus(totalDespesaAdicional())
            .setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Quantidade de itens do documento - base do rateio de frete da venda futura.
     *
     * O frete e calculado por quantidade (a formula da regiao multiplica pela quantidade), entao
     * o rateio tem que ser pela mesma grandeza: retirar 1 item de 100 leva 1% do frete,
     * independente do valor daquele item.
     */
    @JsonIgnore
    fun quantidadeProdutos(): BigDecimal {
        return DocumentLines.fold(BigDecimal.ZERO) { acc, linha ->
            acc.plus(BigDecimal(linha.Quantity))
        }
    }

    //Devolucao abate do que o contrato ja entregou/cobrou; nota de entrega soma.
    @JsonIgnore
    fun sinalNoContrato(): Int {
        return if(docObjectCode == DocumentTypes.oCreditNotes) -1 else 1
    }


    fun presumeDesonerado(rate : Double) : Double {
        return DocumentLines.sumOf { it.presumeDesonerado(rate) }
    }

    @JsonIgnore
    override fun getReconciliationRows(debOrCredt: DebOrCredt): List<ReconciliationRow> {
        val transId = this.TransNum ?: throw Exception("Nao existe numero de transaction")
        val installments = this.documentInstallments
        // Com parcelas (uma ou várias): cada parcela é uma linha da transação (transRowId = índice).
        if (!installments.isNullOrEmpty())
            return installments.mapIndexed { index, it ->
                it.getReconciliationRow(transId, index, this.CardCode)
            }
        // Sem parcelas (ex.: devolução): reconcilia o documento inteiro numa única linha (row 0).
        val total = this.DocTotal?.toDoubleOrNull()
            ?: throw Exception("Documento sem parcelas e sem DocTotal para reconciliar")
        return listOf(InstallmentRow(transId, 0, total, this.CardCode))
    }

    override fun getId(): String {
        return this.docEntry.toString()
    }

    override fun toString(): String {
        return "Document(CardCode='$CardCode', Branch='$BPL_IDAssignedToInvoice', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }

    fun setPix(request: RequestPixDueDate, chave: DataRetonroPixQrCode): Installment? {
        if(request.docEntry() != docEntry)
            throw Exception("O qrCode nao pertence a esse documento")
        return this.documentInstallments
            ?.find { it.InstallmentId == request.getInstallmentId() }
            ?.setPix(request, chave)
    }

    fun setPix(request: RequestPixImmediate, chave: DataRetonroPixQrCode): Installment? {
        if(docEntry != null && request.docEntry() != docEntry)
            throw Exception("O qrCode nao pertence a esse documento")
        val installmentId = request.getInstallmentId() as Int
        val installment = this.documentInstallments?.find { it.InstallmentId == installmentId }
            ?: this.documentInstallments?.firstOrNull().takeIf { docEntry == null && installmentId == 0 }
        return installment?.setPix(request, chave)
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
            pt.BaseType = this.docObjectCode?.value?: throw Exception("Sem object type")
            pt
        }

    }

}
