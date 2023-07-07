package br.andrew.sap.model.uzzipay

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
    val qrCodePurposeType : String = "PDV"
) {

    fun getAmount(): String {
        return "%.2f".format(Locale.ENGLISH, amount)
    }
}

enum class Type(val type: String) {
    TaxId("TaxId"),
    Email("Email"),
    PhoneNumber("PhoneNumber"),
    EVP("EVP")
}