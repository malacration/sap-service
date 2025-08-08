package br.andrew.sap.controllers

import br.andrew.sap.model.estoque.Item
import br.andrew.sap.model.sap.price.ItemPrice
import br.andrew.sap.services.stock.ItemsService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("barter")
class BarterController(val service: ItemsService) {

    @GetMapping()
    fun getPrecoBarter(@RequestParam(name = "itemCode", defaultValue = "GRA0000004") itemCode : String): ItemPrice? {
        return service.getById(itemCode).tryGetValue<Item>().itemPrices.filter { it.PriceList == 61 }.firstOrNull()
    }
}