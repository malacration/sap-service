package br.andrew.sap.controllers


import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.Version
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.DummyService
import br.andrew.sap.services.bank.VendorPaymentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class IndexController(val env: SapEnvrioment,
                      val restTemplate: RestTemplate,
                      val authService: AuthService,
                      val version : Version,
                      val vendorPaymentService: VendorPaymentService,
                      val service: DummyService) {

    @GetMapping("/")
    fun index() : Version{
        return version
    }
}