package br.andrew.sap.controllers.address

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.address.State
import br.andrew.sap.services.address.StateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("state")
class StateController(val stateService : StateService) {

    @GetMapping
    fun get(): List<State> {
        return stateService.getAllCached(Filter(Predicate("Country", "BR",Condicao.EQUAL)))
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): OData {
        return stateService.get(Filter(Predicate("Code", id,Condicao.EQUAL)))
    }
}