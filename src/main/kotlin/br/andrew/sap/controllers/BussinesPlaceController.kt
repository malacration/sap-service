package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.Cancelled
import br.andrew.sap.services.BussinessPlaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("place")
class BussinesPlaceController(val bussinesPlaceService: BussinessPlaceService) {

    @GetMapping()
    fun get(): List<BussinessPlace> {
        return bussinesPlaceService
            .get(Filter(Predicate("Disabled",Cancelled.tNO,Condicao.EQUAL)))
            .tryGetValues<BussinessPlace>()
    }
}