package br.andrew.sap.controllers

import br.andrew.sap.model.Branch
import br.andrew.sap.services.sap.BranchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("branch")
class BranchController(val branchService : BranchService) {

    @GetMapping()
    fun get(): List<Branch> {
        //TODO colocar id Vendedor depois
        return branchService.test(27)
    }
}

