package br.andrew.sap.controllers

import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.sap.BranchService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("branch")
class BranchController(val branchService : BranchService) {

    val logger = LoggerFactory.getLogger(BranchController::class.java)

    @GetMapping()
    fun get(auth : Authentication): ResponseEntity<List<Branch>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        logger.info("Buscando filiais para ${auth.getIdInt()}")
        return ResponseEntity.ok(branchService.getFilialBy(auth.getIdInt()))
    }

    @GetMapping("searchbranch")
    fun searchSustenBranch(): List<Branch> {
        val teste =  branchService.searchBranchLimited()
        return teste
    }
}

