package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.Localidade
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.services.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("locais")
class LocalidadeController(
    val service : LocalidadeService
) {

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): Localidade {
        return service.getById("'$id'").tryGetValue()
    }

    @PostMapping("search")
    fun search(@RequestBody keyWord : String, auth : Authentication): NextLink<Localidade> {
        if(auth is User)
            return service.fullSearchTextFallBack(keyWord,auth)
        return NextLink(listOf(),"")
    }
}
