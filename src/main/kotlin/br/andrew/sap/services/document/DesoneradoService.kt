package br.andrew.sap.services.document

import br.andrew.sap.model.*
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        @Value("\${imposto.icms.desonerado}") val tipoImposto : List<Int>,
                        @Value("\${imposto.icms.desonerado.futuro}")val tipoImpostoFuturo : List<Int>,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService){

    val allImpotos = mutableListOf<Int>().also {
        it.addAll(tipoImposto)
        it.addAll(tipoImpostoFuturo)
    }

    fun aplicaDesonerado(order : Document): Document {
        order.productsByTax().forEach{
            val taxCodeDesonerado = taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines.firstOrNull { allImpotos.contains(it.STAType) }
            if(taxCodeDesonerado != null) {
                var taxParam = taxAuthoritiesService.get(taxCodeDesonerado)
                    .tryGetValue<SalesTaxAuthorities>()
                it.value.forEach { p ->
                    p.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p, taxParam).toString()
                    if(tipoImpostoFuturo.contains(taxCodeDesonerado.STAType))
                        p.valorDesonerado = taxParam.valorImposto(p)
                }
            }
        }
        order.aplicaDescontoDesonerado()
        order.u_pedido_update = "0"
        return order
    }
}