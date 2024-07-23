package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.Attachment
import br.andrew.sap.model.ContactOpaque
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.partner.*
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.ReferenciaComercial
import br.andrew.sap.services.*
import br.andrew.sap.services.security.OneTimePasswordService
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
@RequestMapping("business-partners")
class BusinessPartnersController(
    val service : BusinessPartnersService,
    val refService : ReferenciaComercialService,
    val atualizacao: AtualizacaoCadastralService,
    val anexoController : AttachmentController,
    val otpService: OneTimePasswordService
) {

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): BusinessPartner {
        return service.getById("'$id'").tryGetValue()
    }

    @PostMapping("search")
    fun search(@RequestBody keyWord : String, auth : Authentication): NextLink<BusinessPartnerSlin> {
        return service.fullSearchTextFallBack(keyWord,auth)
    }

    @GetMapping("/key/{hashcode}")
    fun getPnByHashUpdate(@PathVariable hashcode : String): BusinessPartner {
        return service.get(Filter(Predicate("U_keyUpdate","$hashcode",Condicao.EQUAL)))
            .tryGetValues<BusinessPartner>()
            .firstOrNull()?.also {
                if(hashcode != it.U_keyUpdate)
                    throw Exception("Hashcode não confere")
                it.referencias =
                    refService.getByCardCode(it.cardCode!!) ?:
                            refService.save(ReferenciaComercial(it.cardCode!!)).tryGetValue()
        } ?: throw Exception("Parceiro não encontrado ou link invalido")
    }

    @PostMapping("/key/{hashcode}")
    fun setHashUpdat(@PathVariable hashcode : String, @RequestBody bp : BusinessPartner): OData? {
        getPnByHashUpdate(hashcode)
        if(bp.referencias != null){
            val ref = bp.referencias!!
            refService.update(ref,"'${ref.Code}'")
        }
        bp.clearDataNotAllowUpdated()
        return service.update(bp,"'${bp.cardCode}'")
    }

    @PostMapping("/key/{hashcode}/attachment")
    fun uploadCardCode(@PathVariable hashcode : String, @RequestParam("file") file : MultipartFile) {
        val pm = getPnByHashUpdate(hashcode)
        val attachment = Attachment(file)
        anexoController.upload(attachment,pm.cardCode!!)
    }

    @GetMapping("/key/{cardCode}/create")
    fun generateKeyLinkForCardCode(@PathVariable cardCode : String): String {
        val bp = service.getById("'$cardCode'")
            .tryGetValue<BusinessPartner>()
        return atualizacao.atualizaCardCode(bp)
    }

    @PostMapping("")
    fun salvar(@RequestBody bp : Cliente): BusinessPartner {
        return service.save(bp.getBusinessPartner()).tryGetValue()
    }

    @PostMapping("save")
    fun save(@RequestBody bp : Cliente): BusinessPartner {
        return salvar(bp)
    }

    @PutMapping("{cardCode}")
    fun update(@RequestBody bp : Cliente, @PathVariable cardCode : String) {
        //TODO fazer update endereco?
        service.update(bp.getUpdateBusinessPartner(cardCode),"'$cardCode'")
    }

    @GetMapping("/cpf-cnpj")
    @PostAuthorize("@authz.acessoCliente(#root)")
    fun getBy(auth : Authentication,
              @RequestParam(name = "type", defaultValue = "cCustomer") tipo : BusinessPartnerType
    ): ResponseEntity<BusinessPartner> {
        return if(auth is User)
            ResponseEntity.ok(service.getByCpfCnpj(auth.id,tipo))
        else
            ResponseEntity.noContent().build()

    }

    @GetMapping("/cpf-cnpj/contact/{cpfCnpj}")
    fun contact(@PathVariable cpfCnpj : String, @RequestParam(name = "type", defaultValue = "C") tipo : BusinessPartnerType): List<ContactOpaque> {
        return service.getByCpfCnpj(cpfCnpj,tipo).getContactOpaque()
    }
}