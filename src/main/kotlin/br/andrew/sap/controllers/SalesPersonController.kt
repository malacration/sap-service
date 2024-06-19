package br.andrew.sap.controller

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.SalesPersonsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sales-person")
class SalesPersonController(
    val salesPersonsService: SalesPersonsService,
    val businessPartnersService: BusinessPartnersService
){
    
    @PostMapping("search")
    fun search(@RequestBody salesPerson1: String, page : Pageable): Page<SalePerson>? {
        return salesPersonsService.search(salesPerson1, page)
    }

    @GetMapping("replace/{origin}/por/{destino}")
    fun resultado3(@PathVariable origin: Int, @PathVariable destino: Int): Boolean {
        val isDestinoAtivo = salesPersonsService.isSalesPersonActive(destino)
        if (isDestinoAtivo) {
            throw Exception("Vendedor de Destino inativo")
        }
        var pagina: Pageable = PageRequest.of(0,20)
        do {
            val resultado = businessPartnersService.findAllBySalePerson(origin,pagina)
            resultado.forEach {
                print(it.cardCode)
                businessPartnersService.modificarVendedor(it,destino)
            }
            pagina = resultado.nextPageable()
        }while (resultado.hasNext())
        return true
    }
}


