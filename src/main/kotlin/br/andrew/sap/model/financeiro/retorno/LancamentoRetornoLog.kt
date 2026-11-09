package br.andrew.sap.model.financeiro.retorno

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Interruptor do lancamento automatico, linha unica do UDO LANC_RET_CFG (Code AUTOMATICO).
 * U_Data ("2026-11-05T09:30:00") marca quando foi ligado: a rotina so lanca retornos com arquivo
 * a partir desse dia, para nao lancar sozinha o passado que ficou pendente.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LancamentoRetornoAutomatico(
    var Code: String? = null,
    var Name: String? = null,
    var U_Ativo: String? = "N",
    var U_Usuario: String? = null,
    var U_Data: String? = null,
)

/** Uma linha do historico (UDO LANC_RET_LOG). */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LancamentoRetornoLog(
    var Code: String? = null,
    var Name: String? = null,
    var U_Retorno: Int? = null,
    //MANUAL (tela), AUTOMATICO (rotina) ou CONFIG (liga/desliga)
    var U_Origem: String? = null,
    //LANCADO, JA_EXISTIA, RECUSADO, ERRO, LIGADO, DESLIGADO
    var U_Resultado: String? = null,
    var U_Lcm: Int? = null,
    var U_Valor: Double? = null,
    var U_Mensagem: String? = null,
    var U_Usuario: String? = null,
    var U_Data: String? = null,
)
