package br.andrew.sap.model.payment

class PaymentMethodDto(
    val payMethCod: Int?,
    val description: String?,
    val branch: Int?,
    val dflAccount: String?,
    val glAccount: String?,
    val bplId: Int?
)