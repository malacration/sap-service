package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.BussinesPlaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("place")
class BussinesPlaceController(val bussinesPlaceService: BussinesPlaceService) {


    @GetMapping()
    fun get(): OData {
        return bussinesPlaceService.get()
    }
}