package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.Condicao
import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.Predicate
import br.andrew.sap.rovema.model.RomaneioPesagem
import br.andrew.sap.rovema.services.UserObjectsMDService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("objects")
class ObjectsController(val userObjectsMDService: UserObjectsMDService) {

    @GetMapping()
    fun getAllObject() : Any{
        return userObjectsMDService.get()
    }

    @GetMapping("{name}")
    fun getObject(@PathVariable name : String) : Any{
        return userObjectsMDService
                .get(Filter(listOf(Predicate("TableName",name,Condicao.STARTS_WITH))))
    }

    @GetMapping("/coluna/{coluna}/start/{valor}")
    fun getObjectComplexo(@PathVariable coluna : String, @PathVariable valor : String ) : Any{
        return userObjectsMDService
                .get(Filter(listOf(Predicate(coluna,valor,Condicao.STARTS_WITH))))
    }
}