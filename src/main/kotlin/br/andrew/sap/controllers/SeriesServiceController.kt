package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.Attachment
import br.andrew.sap.model.ContactOpaque
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.ReferenciaComercial
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.*
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.security.OneTimePasswordService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("pedro")
class SeriesServiceController(
    val service : SeriesService
) {

    @GetMapping()
    fun get(): OData {
        return service.get()
    }
}