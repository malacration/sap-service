package br.andrew.sap.services.document

import br.andrew.sap.model.*
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.stereotype.Service

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService){


    fun aplicaDesonerado(order : Document): Document {
        order.productsByTax().forEach{
            val taxCodeDesonerado = taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines.firstOrNull { it.STAType == 25 || it.STAType == 28 }
            if(taxCodeDesonerado != null) {
                var taxParam = taxAuthoritiesService.get(taxCodeDesonerado)
                    .tryGetValue<SalesTaxAuthorities>()
                it.value.forEach { p ->
                    p.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p, taxParam).toString()
                    if(taxCodeDesonerado.STAType == 28)
                        p.valorDesonerado = taxParam.valorImposto(p)
                }
            }
        }
        order.aplicaDescontoDesonerado()
        order.u_pedido_update = "0"
        return order
    }
}