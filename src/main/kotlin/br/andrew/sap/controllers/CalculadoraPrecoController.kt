package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.pricing.PriceListsService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("calculadora-preco")
class CalculadoraPrecoController(
    val itemService: ItemsService,
    val productTreesService: ProductTreesService,
    val priceListService : PriceListsService
) {

    @GetMapping("/itens/all")
    fun get(
        @RequestParam(name = "itemCodePrefix", defaultValue = "PAC") itemCodePrefix: String,
        @RequestParam(name = "warehouse") warehouse: String,
        page : Pageable,
        auth : Authentication): List<Produto> {
        val filter = Filter(
            Predicate("ItemCode",itemCodePrefix, Condicao.STARTS_WITH),
            Predicate("TreeType","iProductionTree", Condicao.EQUAL),
            Predicate("Valid", YesNo.tYES, Condicao.EQUAL)
        )
        return itemService.get(filter).tryGetValues<Produto>().also {
            it.forEach{
                it.defaultWareHouse = warehouse
                it.kgsPorUnidade = BigDecimal(33.33)
                it.custoGgf = BigDecimal(3.5)
        }}
    }

    @GetMapping("/product-trees-service/all")
    fun productTree(
        @RequestParam(name = "itemCodePrefix", defaultValue = "PAC") itemCodePrefix: String,
        page : Pageable,
        auth : Authentication): OData {
        val filter = Filter(
            Predicate("TreeCode",itemCodePrefix, Condicao.STARTS_WITH)
        )
        return productTreesService.get(filter)
    }

}