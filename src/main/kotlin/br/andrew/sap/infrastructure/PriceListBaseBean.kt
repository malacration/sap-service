package br.andrew.sap.infrastructure

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.price.PriceList
import br.andrew.sap.services.PriceListsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PriceListBaseBean(val priceListsService: PriceListsService) {


    @Bean(name = arrayOf("base.price.list.id"))
    fun getBasePriceList(@Value("\${base.price.list.name:none}") baseListName : String): String {
        if(baseListName == "none"){
            println("Nenhuma lista de preço informada, 0.0 sempre sera registrado")
            return "none"
        }
        val filter = Filter(listOf(Predicate("PriceListName",baseListName,Condicao.EQUAL)))
        val price = priceListsService.get(filter).tryGetValues<PriceList>().firstOrNull()
        return price?.priceListNo?.toString() ?: throw Exception("Lista de preço $baseListName não foi encontrada")
    }
}