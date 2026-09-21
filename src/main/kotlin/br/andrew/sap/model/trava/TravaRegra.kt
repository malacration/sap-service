package br.andrew.sap.model.trava

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Catálogo de regras de liberação de trava, armazenado no SAP como UDO
 * `TRAVA_REGRA` (/b1s/v1/TRAVA_REGRA). `Code` é a chave e também o código da
 * regra usado no OTP e no literal da TransactionNotification (ex.: DESCONTO).
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TravaRegra(
    var Code: String? = null,
    var U_Descricao: String? = null,
    var U_Ordem: Int? = null,
    var U_Ativo: String? = "Y"
)
