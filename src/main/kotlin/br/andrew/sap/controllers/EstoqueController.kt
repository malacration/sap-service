package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.estoque.Item
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.stock.StockTakingsService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
TODO atividades estoque
remover estoques da fazenda dos pacs
remover
 */

@RestController
@RequestMapping("estoque")
class EstoqueController(
    val service: ItemsService,
    val stockTakingService: StockTakingsService,
    ){

    val logger = LoggerFactory.getLogger(EstoqueController::class.java)

    @GetMapping()
    fun index() : Any?{
        val filter = Filter("ItemCode","PAC",Condicao.STARTS_WITH)
        val itens = service.getAll(Item::class.java,filter)
        itens.sortedBy { it.itemCode }.parallelStream().forEach{ item ->
            try {
                item.ItemWarehouseInfoCollection
                    ?.filter { it.WarehouseCode.toCharArray()[0] != '5' }
                    ?.parallelStream()?.forEach { stockTakingService.delete(item.itemCode,it.WarehouseCode) }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        return "item"
    }
}
