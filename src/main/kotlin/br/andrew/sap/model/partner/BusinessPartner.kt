package br.andrew.sap.model.partner

import br.andrew.sap.model.PaymentMethod
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BusinessPartner() {
    constructor(cardName : String,
                type: BusinessPartnerType) : this() {
        this.cardName = cardName;
        this.cardCode = cardCode
        this.cardType = type
    }

    fun setCpfCnpj(cpfCnpj: CpfCnpj, ierg: String? = null) {
        this.BPFiscalTaxIDCollection = listOf(BPFiscalTaxID(cpfCnpj,ierg))
    }



    var freeText: String? = null
    var u_id_forca: String? = null
    var emailAddress: String? = null
    var phone1: String? = null
    var cardName : String? = null;
    var cardType : BusinessPartnerType? = null;
    var cardCode : String? = null
    var salesPersonCode : Int? = null
    var U_fazer_fluxo_prazo : String? = "0"
//    var BPLID : String? = null
//    var DisabledForBP : String? = null
    var series : Int? = 77

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
}