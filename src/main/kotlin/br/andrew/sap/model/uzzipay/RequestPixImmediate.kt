package br.andrew.sap.model.uzzipay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class RequestPixImmediate(
    val externalIdentifier: String,
    private val conta : ContaUzziPayPix,
    private val amount : BigDecimal,
    private val cnpj: String,
    val expiration: Int,
    val keyType : Type = Type.EVP,
    val qrCodePurposeType : String = "PDV",
) {


    val key = conta.chavePix

    @JsonIgnoreProperties
    fun getContaSelecioanda(contas : List<ContaUzziPayPix>): ContaUzziPayPix {
        return contas.first { it.cnpj == conta.cnpj }
    }

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