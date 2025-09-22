package br.andrew.sap.controllers

import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("branch")
class BranchController(
    val service : BussinessPlaceService,
    val businesPlaceService: BussinessPlaceService
) {

    val logger = LoggerFactory.getLogger(BranchController::class.java)

    @GetMapping()
    fun get(auth : Authentication): ResponseEntity<List<Branch>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        logger.info("Buscando filiais para ${auth.getIdInt()}")
        if(auth.superVendedor() > -1)
            return ResponseEntity.ok(service.getAll(Branch::class.java))
        return ResponseEntity.ok(service.getFilialBy(auth.getIdInt()))
    }
}

