package br.andrew.sap.model.comercial
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class VendaMensal(
    val Ano: Int,
    val Mes: Int,
    val Total: BigDecimal,
    val Qtde: Int
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class VendaDetalhe(
    val DocEntry: Int,
    val DocNum: Int,
    val CardCode: String?,
    val CardName: String?,
    val DocDate: String?,
    val DocTotal: BigDecimal
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class VendaProduto(
    val ItemCode: String?,
    val Description: String?,
    val Quantidade: BigDecimal,
    val Total: BigDecimal
)
