package br.andrew.sap.controllers


import br.andrew.sap.services.bacen.BacenOlindaService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("bacen")
class BacenController(val service: BacenOlindaService){

    val logger = LoggerFactory.getLogger(BacenController::class.java)

    @GetMapping()
    fun index() : Any{
        return service.cotacaoMoeda("USD")
    }
}
