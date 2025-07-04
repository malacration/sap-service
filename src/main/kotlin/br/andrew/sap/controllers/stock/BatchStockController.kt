package br.andrew.sap.controllers.stock

import br.andrew.sap.services.stock.BatchStockService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("batch-stock")
class BatchStockController(val service: BatchStockService) {

    @GetMapping("/item-code/{id}/stock/{stock}")
    fun get(@PathVariable id : String, @PathVariable stock : String) : List<Any>? {
        return service.get(id,stock)?.tryGetValues<Any>()
    }
}