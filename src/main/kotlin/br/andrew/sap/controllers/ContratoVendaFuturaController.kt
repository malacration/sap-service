package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.Version
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.entity.FormColumns
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.abstracts.TesteService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("contrato-venda-futura")
class ContratoVendaFuturaController(val service : ContratoVendaFuturaService){

    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        val contratos = service.get(Filter("U_vendedor",auth.getIdInt(),Condicao.EQUAL),page)
            .tryGetPageValues<Contrato>(page)
        return ResponseEntity.ok(contratos)
    }
}
