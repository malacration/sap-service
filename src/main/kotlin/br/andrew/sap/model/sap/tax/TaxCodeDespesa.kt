package br.andrew.sap.model.sap.tax

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** Uma linha da view `tax-code-despesa.sql` - a despesa adicional e o codigo de imposto dela. */
@JsonIgnoreProperties(ignoreUnknown = true)
class TaxCodeDespesa(val DocEntry: Int? = null,
                     val LineNum: Int? = null,
                     val ExpnsCode: Int? = null,
                     val TaxCode: String? = null)
