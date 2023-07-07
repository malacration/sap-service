package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.Installment
import br.andrew.sap.model.partner.Address
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

class RequestQrCode(
    val externalIdentifier: String,
    val key : String,
    val keyType : Type,
    private val amount : BigDecimal,
    val dueDate : String,
    val Payer : Payer,
    private val cnpj: String,
    val qrCodePurposeType : String = "PDV",
) {

    @JsonIgnoreProperties
    fun getCnpj() : String {
        return cnpj
    }
    fun getAmount(): String {
        return "%.2f".format(Locale.ENGLISH, amount)
    }

    @JsonIgnoreProperties
    fun getInstallmentId(): Any {
        return externalIdentifier.split("-")[2].split(":")[1].toInt()
    }

    fun docEntry(): Int {
        return externalIdentifier.split("-")[1].replace("Entry","").toInt()
    }

    fun docType(): String {
        return externalIdentifier.split("-")[3]
    }

    fun docNum(): String {
        return externalIdentifier.split("-")[0].replace("Num","")
    }
}

enum class Type(val type: String) {
    TaxId("TaxId"),
    Email("Email"),
    PhoneNumber("PhoneNumber"),
    EVP("EVP")
}