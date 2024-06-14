package br.andrew.sap.model.partner

import br.andrew.sap.model.ContactOpaque
import br.andrew.sap.model.bank.PaymentMethod
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.security.MessageDigest
import java.util.Date


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class BusinessPartner() {
    constructor(cardName : String,
                type: BusinessPartnerType) : this() {
        this.cardName = cardName
        this.cardType = type
    }

    fun setCpfCnpj(cpfCnpj: CpfCnpj, ierg: String? = null) {
        this.BPFiscalTaxIDCollection = listOf(BPFiscalTaxID(cpfCnpj,ierg))
    }

    @JsonIgnore
    fun getCpfCnpj() : CpfCnpj {
        return BPFiscalTaxIDCollection
            ?.filter { (it.TaxId0 != null && it.TaxId0!!.isNotBlank()) || (it.TaxId4 != null && it.TaxId4!!.isNotBlank()) }
            ?.map { CpfCnpj(it) }
            ?.first() ?: throw Exception("CpfCnpj not found")
    }

    var referencias: ReferenciaComercial? = null
    var freeText: String? = null
    var u_id_forca: String? = null
    var emailAddress: String? = null
    var phone1: String? = null
    var phone2: String? = null
    var cardName : String? = null
    var cardType : BusinessPartnerType? = null
    var cardCode : String? = null
    var salesPersonCode : Int? = null
    var U_fazer_fluxo_prazo : String? = "0"
    var attachmentEntry : Int? = null
    var CreateDate : String? = null
    var SlpCode : Int? = null

    var RemoveContacts : List<Int>? = null
//    var BPLID : String? = null
//    var DisabledForBP : String? = null
    var series : Int? = 77
    var ContactEmployees : MutableList<Person> = mutableListOf()
    var U_Rov_Data_Nascimento : String? = null
    var U_Rov_Nome_Mae : String? = null

    var U_keyUpdate : String? = null
    var U_dataSerasa : Date? = null



    @JsonProperty("BPBranchAssignment")
    var bpBranchAssignment : List<BPBranchAssignment>? = null

    @JsonIgnoreProperties
    private var BPFiscalTaxIDCollection : List<BPFiscalTaxID>? = null
    @JsonIgnoreProperties
    private var BPPaymentMethods : List<PaymentMethod> = listOf()
    @JsonIgnoreProperties
    private var BPAddresses : List<Address> = listOf()

    @JsonProperty("BPPaymentMethods")
    fun getBPPaymentMethods(): List<PaymentMethod> {
        return BPPaymentMethods
    }

    @JsonProperty("BPPaymentMethods")
    fun setBPPaymentMethods(valor : List<PaymentMethod>) {
        BPPaymentMethods = valor
    }

    @JsonProperty("BPFiscalTaxIDCollection")
    fun getBPFiscalTaxIDCollection(): List<BPFiscalTaxID>? {
        return BPFiscalTaxIDCollection
    }
    @JsonProperty("BPFiscalTaxIDCollection")
    fun setBPFiscalTaxIDCollection(valor : List<BPFiscalTaxID>) {
        BPFiscalTaxIDCollection = valor
    }

    @JsonProperty("BPAddresses")
    fun getAddresses(): List<Address> {
        return BPAddresses
    }

    @JsonProperty("BPAddresses")
    fun setAddresses(valor : List<Address>) {
        BPAddresses = valor
    }

    fun setAddresse(valor : Address) {
        BPAddresses = listOf(
            valor.also { it.addressType = AddresType.bo_ShipTo },
            valor.duplicate().also {
                it.addressName = null
                it.addressType = AddresType.bo_BillTo }
        )

    }

    override fun toString(): String {
        return "$cardCode - CNPJ($BPFiscalTaxIDCollection)"
    }

    fun clearDataNotAllowUpdated() {
        this.bpBranchAssignment = null
        BPFiscalTaxIDCollection = null
        BPPaymentMethods = listOf()
        this.RemoveContacts = null
        this.referencias = null
        this.U_keyUpdate = ""
    }

    fun generateKey(md : MessageDigest): String {
        val timesTamp = "${cardCode}-${System.currentTimeMillis()}"
        val bytes = timesTamp.toByteArray()
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    @JsonIgnore
    fun getContactOpaque(): List<ContactOpaque> {
        return listOf(
            ContactOpaque(emailAddress?: "")
        )
    }
}
