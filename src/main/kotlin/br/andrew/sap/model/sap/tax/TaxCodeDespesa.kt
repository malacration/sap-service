package br.andrew.sap.model.sap.tax

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Uma linha da view `tax-code-despesa.sql`: o rateio de uma despesa adicional numa linha de
 * produto. [LineNum] e a linha de PRODUTO que recebeu o rateio, [LineTotal] a parcela que
 * coube a ela e [TaxCode] o codigo de imposto dessa parcela.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class TaxCodeDespesa(val DocEntry: Int? = null,
                     val LineNum: Int? = null,
                     val ExpnsCode: Int? = null,
                     val LineTotal: Double? = null,
                     val TaxCode: String? = null)
