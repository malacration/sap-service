package br.andrew.sap.model.uzzipay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class RequestPixDueDate(
    val externalIdentifier: String,
    private val conta : ContaUzziPayPix,
    private val amount : BigDecimal,
    private val dueDate : LocalDate,
    val Payer : Payer,
    private val cnpj: String,
    val keyType : Type = Type.EVP,
    val qrCodePurposeType : String = "BILLET",
) {
    constructor(externalIdentifier: String,
                conta: ContaUzziPayPix,
                amount: BigDecimal,
                dueDate: String, Payer: Payer, cnpj: String,
                keyType: Type = Type.EVP) :
            this(externalIdentifier,conta,amount,
                LocalDate.parse(dueDate,DateTimeFormatter.ofPattern("yyy-MM-dd")),
                Payer,
                cnpj,keyType)

    init{
        if (LocalDate.now() > dueDate)
            throw Exception("Data de vencimento não pode ser menor que a data atual")

    }
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

    fun getDueDate(): String {
            if (LocalDate.now() == dueDate)
                return SimpleDateFormat("yyyy-MM-dd")
                    .format(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
        return dueDate.toString();

    }
}

enum class Type(val type: String) {
    TaxId("TaxId"),
    Email("Email"),
    PhoneNumber("PhoneNumber"),
    EVP("EVP")
}