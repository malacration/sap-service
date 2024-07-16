package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/sales-tax")
class SalesTaxCodeController(val salesTaxCodeService: SalesTaxCodeService) {


    @GetMapping()
    fun get(pageable : Pageable) : OData {
        return salesTaxCodeService.get(Filter(),pageable)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id : String) : OData {
        return salesTaxCodeService.getById(id)
    }
}