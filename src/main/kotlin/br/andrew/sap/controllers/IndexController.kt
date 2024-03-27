package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.Version
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.*
import br.andrew.sap.services.bank.*
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class IndexController(val version : Version,
                      val service : BusinessPartnersService){

    @GetMapping("/")
    fun index() : Version{
        return version
    }

    @GetMapping("/teste")
    fun teste() : Any {
        var pagina = service.get(Filter(Predicate("CardType", "C",Condicao.EQUAL)))
        var qtd = 0
        while (pagina.hasNext()) {
            try {
                pagina.tryGetValues<BusinessPartner>().filter {
                    it.getAddresses().filter { !it.isValid() }.isNotEmpty()
                }.forEach {
                    println("Atualizando cliente ${it.cardCode} - ${it.cardName}")
                    service.normalizeAddressName(it)
                    qtd++
                }
            } catch (t: Throwable) {
                println(t.message)
            }
            pagina = service.next(pagina)
        }

        return qtd
    }
}
