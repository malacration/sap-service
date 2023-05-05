package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.partner.*
import br.andrew.sap.services.BusinessPartnersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("business-partners")
class BusinessPartnersController(val service : BusinessPartnersService) {


    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @PostMapping("save")
    fun save(bp : BusinessPartner): OData? {
        return service.save(bp)
    }

//    @GetMapping("teste")
//    fun update(): OData? {
//        val bp : BusinessPartner = BusinessPartner(
//                "Windson",
//                BusinessPartnerType.C).also {
//                    it.setCpfCnpj(CpfCnpj("01847004261"))
//                    it.setAddresses(Address().also {
//                        it.AddressType = AddresType.bo_BillTo
//                        it.addressName = "Endereço de cobrança"
//                        it.Street = "Rua dos bobos"
//                        it.Block = "Bloco 1"
//                        it.ZipCode = "12345678"
//                        it.City = "São Paulo"
//                        it.County = "Brasil"
//                        it.Country = "BR"
//                        it.State = "SP"
//                        it.BuildingFloorRoom = "Sala 1"
//                        it.TypeOfAddress = "Rodovia"
//                        it.StreetNo = "123"
//                    })
//        }
//        return service.save(bp)
//    }
}
