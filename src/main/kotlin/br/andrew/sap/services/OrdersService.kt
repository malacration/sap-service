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
        val orderFor = if (order.docEntry != null && order.productsByTax().isEmpty())
            this.getById(order.docEntry!!).tryGetValue<OrderSales>()
        else
            order
        orderFor.productsByTax().forEach{
            var taxParam = taxAuthoritiesService.get(taxCodeService.getById(it.key).tryGetValue<SalesTaxCode>()
                    .salesTaxCodes_Lines.filter { it.STAType == 25 }.first())
                    .tryGetValue<SalesTaxAuthorities>()
            it.value.forEach { p ->
                p.UnitPrice =  PrecoUnitarioComDesoneracao().calculaPreco(p.UnitPrice,taxParam).toString() }
        }
        return order
    }
}