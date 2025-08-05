package br.andrew.sap.controllers.stock

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.services.stock.BatchNumberService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("batch-number")
class BatchNumberController(val service: BatchNumberService) {


    @GetMapping()
    fun get() : Any{
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Any{
        return service.get(Filter("ItemCode",id,Condicao.EQUAL))
    }
}