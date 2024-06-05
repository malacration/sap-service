package br.andrew.sap.controllers


import br.andrew.sap.model.CurrencyRate
import br.andrew.sap.services.CompanyService
import br.andrew.sap.services.bacen.BacenOlindaService
import br.andrew.sap.services.bacen.CotacaoMoedaParse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("currency")
class CurrencyController(
    val service: CompanyService,
    val bacenOlindaService: BacenOlindaService){

    val logger = LoggerFactory.getLogger(CurrencyController::class.java)

    @GetMapping()
    fun index() : String? {
        return service.getCurrencyRate()
    }


    @GetMapping("update/by-bacen")
    fun atualizaRateBacen() : Any{
        val currency = "USD"
        val lista = bacenOlindaService.cotacaoMoeda(currency)
        return service.setCurrencyRate(CotacaoMoedaParse().toCurrencyRateRange(lista,currency,31))
    }
}
