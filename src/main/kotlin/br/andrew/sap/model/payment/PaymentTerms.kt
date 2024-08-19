package br.andrew.sap.model.payment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PaymentTermsTypes(){
    var GroupNumber: Int? = null
    var PaymentTermsGroupName: String? = null
    var StartFrom: String? = null
    var NumberOfAdditionalMonths: Int? = null
    var NumberOfAdditionalDays: Int? = null
    var CreditLimit: Double? = null
    var GeneralDiscount: Double? = null
    var InterestOnArrears: Double? = null
    var PriceListNo: Int? = null
    var LoadLimit: Double? = null
    var OpenReceipt: String? = null
    var DiscountCode: String? = null
    var DunningCode: String? = null
    var BaselineDate: String? = null
    var NumberOfInstallments: Int? = null
    var NumberOfToleranceDays: Int? = null
    var U_LbrAmfsTipoCondicao: String? = null
    var U_ROV_Desconto: Int? = null
    var U_ROV_Acrecimo: Double? = null
    var U_Rov_EnviarForca: String? = null
}