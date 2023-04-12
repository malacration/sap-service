package br.andrew.sap.services

import br.andrew.sap.model.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.SalesTaxAuthorities
import br.andrew.sap.model.SalesTaxCode
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrdersService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
                    val taxCodeService: SalesTaxCodeService,
                    val taxAuthoritiesService: SalesTaxAuthoritiesService) :
        EntitiesService<OrderSales>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/Orders"
    }

    fun aplicaDesonerado(order : OrderSales): OrderSales {
        order.productsByTax().forEach{
            val taxCodeDesonerado = taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                    .salesTaxCodes_Lines.firstOrNull { it.STAType == 25 }
            if(taxCodeDesonerado != null) {
                var taxParam = taxAuthoritiesService.get(taxCodeDesonerado)
                        .tryGetValue<SalesTaxAuthorities>()
                it.value.forEach { p ->
                    p.unitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p.unitPrice, taxParam).toString()
                }
            }
        }
        order.u_pedido_update = "0";
        update(order,order.docEntry.toString())
        return order
    }
}