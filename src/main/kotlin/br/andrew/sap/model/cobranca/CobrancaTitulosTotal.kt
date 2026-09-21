package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

/**
 * Total do filtro inteiro da tela de titulos, nao so da pagina carregada.
 *
 * Sem @JsonInclude(NON_EMPTY) de proposito, diferente dos vizinhos: aqui zero e resposta valida
 * ("o filtro nao achou nada") e Truncado=false precisa viajar. Com NON_EMPTY os dois sumiriam do
 * JSON e a tela leria "sem total" onde na verdade o total e zero.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class CobrancaTitulosTotal(
    val Parcelas: Int,
    val Saldo: BigDecimal,
    val ValorPago: BigDecimal,
    val ParcelasComPagamento: Int,
    /** Parou no teto de paginas do SAP: o total e parcial e a tela precisa avisar. */
    val Truncado: Boolean,
)
