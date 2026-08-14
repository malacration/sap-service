package br.andrew.sap.controllers.cadastro

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.sistema.Attachment
import br.andrew.sap.model.cadastro.ContactOpaque
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.dto.ContasReceberDto
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerCrossJoin
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.BPFiscalTaxID
import br.andrew.sap.model.sap.partner.CpfCnpj
import br.andrew.sap.model.sap.partner.ReferenciaComercial
import br.andrew.sap.services.*
import br.andrew.sap.services.abstracts.Entidade
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.documents.QuotationsService
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
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.cadastro.ReferenciaComercialService
import br.andrew.sap.services.cadastro.AtualizacaoCadastralService
import br.andrew.sap.services.cadastro.SalesPersonsService
import br.andrew.sap.controllers.sistema.AttachmentController

@RestController
@RequestMapping("business-partners")
class BusinessPartnersController(
    val service : BusinessPartnersService,
    val orderService : OrdersService,
    val quotationsService: QuotationsService,
    val refService : ReferenciaComercialService,
    val atualizacao: AtualizacaoCadastralService,
    val anexoController : AttachmentController,
    val otpService: OneTimePasswordService,
    val salesPersonsService: SalesPersonsService
) {

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): BusinessPartner {
        return service.getById("'$id'").tryGetValue()
    }

    @PutMapping("{cardCode}/enderecos/{addressName}/localidade")
    fun atualizaLocalidadeEndereco(@PathVariable cardCode : String,
                                   @PathVariable addressName : String,
                                   @RequestParam(name = "localidade", required = false) localidade : Int?): BusinessPartner {
        //0 limpa o vinculo, o service layer nao aceita null em campo numerico de usuario
        return service.atualizaLocalidadeEndereco(cardCode, addressName, localidade ?: 0)
    }

    @PostMapping("search")
    fun search(@RequestBody keyWord : String, auth : Authentication): NextLink<BusinessPartnerSlin> {
        if(auth is User)
            return service.fullSearchTextFallBack(keyWord,auth)
        return NextLink(listOf(),"")
    }

    @GetMapping("search-vendedor")
    fun searchVendedor(@RequestParam search: String): List<BusinessPartnerSlin> {
        return service.searchBusinessPartners(search)
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

    @GetMapping("/cientes")
    fun get(auth: Authentication,
            @RequestParam(required = false) cardCode: String?,
            @RequestParam(required = false) cardName: String?,
            @RequestParam(required = false) cpfCnpj: String?,
            @RequestParam(required = false) salesPersonCode: Int?,
            page: Pageable): ResponseEntity<Page<BusinessPartner>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()

        val entidades = mutableListOf(
            Entidade("BusinessPartners",listOf("CardCode","CardName","SalesPersonCode","Phone1","Phone2","CardType","Valid","CreditLine")),
        )
        val filter = Filter(listOf(
            Predicate("BusinessPartners/CardType", "C", Condicao.EQUAL),
            Predicate("BusinessPartners/Valid", YesNo.tYES, Condicao.EQUAL)
        ))

        if(!auth.isListAllBusinessPartner()){
            filter.add(Predicate("BusinessPartners/SalesPersonCode", auth.getIdInt(), Condicao.EQUAL))
        } else if(salesPersonCode != null) {
            filter.add(Predicate("BusinessPartners/SalesPersonCode", salesPersonCode, Condicao.EQUAL))
        }
        if (!cardName?.trim().isNullOrEmpty()) {
            filter.add(Predicate("BusinessPartners/CardName", cardName.trim(), Condicao.CONTAINS))
        }
        if (!cardCode?.trim().isNullOrEmpty()) {
            filter.add(Predicate("BusinessPartners/CardCode", cardCode.trim(), Condicao.EQUAL))
        }
        return if (!cpfCnpj?.trim().isNullOrEmpty()) {
            entidades.add(Entidade("BusinessPartners/BPFiscalTaxIDCollection",listOf("TaxId4","TaxId0","BPCode")))

            filter.add(Predicate("BusinessPartners/CardCode", "BusinessPartners/BPFiscalTaxIDCollection/BPCode", Condicao.EQUAL,true))
            val cpfCnpjObj = CpfCnpj(cpfCnpj)
            if(cpfCnpjObj.isCnpj())
                filter.add(Predicate("BusinessPartners/BPFiscalTaxIDCollection/TaxId0", cpfCnpjObj.toString(), Condicao.EQUAL))
            else
                filter.add(Predicate("BusinessPartners/BPFiscalTaxIDCollection/TaxId4", cpfCnpjObj.toString(), Condicao.EQUAL))
            val resultado = service.crossJoin(entidades, filter, OrderBy(),page)
                    .tryGetPageValues<BusinessPartnerCrossJoin>(page).map {
                        if(it.BPFiscalTaxIDCollection != null)
                            it.BusinessPartners.setBPFiscalTaxIDCollection(listOf(it.BPFiscalTaxIDCollection!!))
                        it.BusinessPartners
                    }
            ResponseEntity.ok(enriqueceListagem(resultado))
        }else{
            val resultado = service.get(
                filter.withStripPrefix("BusinessPartners/"),
                OrderBy(mapOf("CardName" to Order.ASC)),
                page
            ).tryGetPageValues<BusinessPartner>(page)
            return ResponseEntity.ok(enriqueceListagem(resultado))
        }
    }

    private fun enriqueceListagem(page: Page<BusinessPartner>): Page<BusinessPartner> {
        val fiscais = buscaFiscais(page.content)
        val vendedores = page.content
            .mapNotNull { it.salesPersonCode }
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.let {
                salesPersonsService.get(Filter(Predicate("SalesEmployeeCode", it, Condicao.IN)))
                    .tryGetValues<br.andrew.sap.model.sap.cadastro.SalePerson>()
                    .associateBy { vendedor -> vendedor.SalesEmployeeCode }
            } ?: emptyMap<Int, br.andrew.sap.model.sap.cadastro.SalePerson>()

        page.content.forEach { parceiro ->
            if(parceiro.TaxId0.isNullOrBlank() && parceiro.TaxId4.isNullOrBlank()){
                val fiscal = parceiro.getBPFiscalTaxIDCollection()?.firstOrNull()
                    ?: parceiro.cardCode?.let { fiscais[it] }
                    ?: runCatching {
                        service.getById("'${parceiro.cardCode}'")
                            .tryGetValue<BusinessPartner>()
                            .getBPFiscalTaxIDCollection()
                            ?.firstOrNull()
                    }.getOrNull()
                parceiro.TaxId0 = fiscal?.TaxId0
                parceiro.TaxId4 = fiscal?.TaxId4
            }
            parceiro.salesEmployeeName = parceiro.salesPersonCode?.let { vendedores[it]?.SalesEmployeeName }
        }
        return page
    }

    private fun buscaFiscais(parceiros: List<BusinessPartner>): Map<String, BPFiscalTaxID> {
        val codigos = parceiros.mapNotNull { it.cardCode }.distinct()
        if(codigos.isEmpty())
            return emptyMap()

        return runCatching<Map<String, BPFiscalTaxID>> {
            val entidades = listOf(
                Entidade("BusinessPartners", listOf("CardCode")),
                Entidade("BusinessPartners/BPFiscalTaxIDCollection", listOf("TaxId4", "TaxId0", "BPCode"))
            )
            val filter = Filter(listOf(
                Predicate("BusinessPartners/CardCode", "BusinessPartners/BPFiscalTaxIDCollection/BPCode", Condicao.EQUAL, true),
                Predicate("BusinessPartners/CardCode", codigos, Condicao.IN)
            ))

            service.crossJoin(entidades, filter, OrderBy(), Pageable.ofSize(codigos.size * 2))
                .tryGetValues<BusinessPartnerCrossJoin>()
                .mapNotNull { it.BPFiscalTaxIDCollection }
                .filter { !it.TaxId0.isNullOrBlank() || !it.TaxId4.isNullOrBlank() }
                .distinctBy { it.TaxId0 ?: it.TaxId4 }
                .associateBy { it.BPCode ?: "" }
                .filterKeys { it.isNotBlank() }
        }.getOrElse { emptyMap() }
    }

    @GetMapping("pedido-venda-parceiro")
    fun get(auth : Authentication,
            @RequestParam CardCode: String,
            @RequestParam(required = false) status: DocumentStatus?,
            @RequestParam(required = false) docNum: Int?,
            @RequestParam(required = false) dataInicial: String?,
            @RequestParam(required = false) dataFinal: String?,
            @RequestParam(required = false) salesPersonCode: Int?,
            page: Pageable): ResponseEntity<Page<OrderSales>> {
        if(!(auth is User))
            return ResponseEntity.noContent().build()
        val documentos = orderService
            .get(
                filtroDocumentosParceiro(CardCode, status, docNum, dataInicial, dataFinal, salesPersonCode),
                OrderBy(mapOf("DocDate" to Order.DESC, "DocEntry" to Order.DESC)),
                page
            )
            .tryGetPageValues<OrderSales>(page)
        return ResponseEntity.ok(enriqueceVendedores(documentos))
    }

    @GetMapping("cotacoes-parceiro")
    fun getCotacoes(auth : Authentication,
                    @RequestParam CardCode: String,
                    @RequestParam(required = false) status: DocumentStatus?,
                    @RequestParam(required = false) docNum: Int?,
                    @RequestParam(required = false) dataInicial: String?,
                    @RequestParam(required = false) dataFinal: String?,
                    @RequestParam(required = false) salesPersonCode: Int?,
                    page: Pageable): ResponseEntity<Page<OrderSales>> {
        if(!(auth is User))
            return ResponseEntity.noContent().build()
        val documentos = quotationsService
            .get(
                filtroDocumentosParceiro(CardCode, status, docNum, dataInicial, dataFinal, salesPersonCode),
                OrderBy(mapOf("DocDate" to Order.DESC, "DocEntry" to Order.DESC)),
                page
            )
            .tryGetPageValues<OrderSales>(page)
        return ResponseEntity.ok(enriqueceVendedores(documentos))
    }

    private fun filtroDocumentosParceiro(cardCode: String,
                                         status: DocumentStatus?,
                                         docNum: Int?,
                                         dataInicial: String?,
                                         dataFinal: String?,
                                         salesPersonCode: Int?): Filter {
        return Filter(
            Predicate("CardCode", cardCode, Condicao.EQUAL),
            Predicate(Cancelled.tNO, Condicao.EQUAL),
        ).also {
            if(status != null)
                it.add(Predicate("DocumentStatus", status.typeName, Condicao.EQUAL))
            if(docNum != null)
                it.add(Predicate("DocNum", docNum, Condicao.EQUAL))
            if(!dataInicial.isNullOrBlank())
                it.add(Predicate("DocDate", dataInicial, Condicao.GREAT_EQUAL))
            if(!dataFinal.isNullOrBlank())
                it.add(Predicate("DocDate", dataFinal, Condicao.LESS_EQUAL))
            if(salesPersonCode != null)
                it.add(Predicate("SalesPersonCode", salesPersonCode, Condicao.EQUAL))
        }
    }

    private fun <T : Document> enriqueceVendedores(page: Page<T>): Page<T> {
        val vendedores = page.content
            .map { it.salesPersonCode }
            .filter { it != -1 }
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.let {
                salesPersonsService.get(Filter(Predicate("SalesEmployeeCode", it, Condicao.IN)))
                    .tryGetValues<br.andrew.sap.model.sap.cadastro.SalePerson>()
                    .associateBy { vendedor -> vendedor.SalesEmployeeCode }
            } ?: emptyMap()
        page.content.forEach { documento ->
            documento.salesEmployeeName = vendedores[documento.salesPersonCode]?.SalesEmployeeName
        }
        return page
    }

    @GetMapping("/cpf-cnpj/teste/{cpfCnpj}")
    fun teste(@PathVariable cpfCnpj : String, @RequestParam(name = "type", defaultValue = "C") tipo : BusinessPartnerType): List<ContactOpaque> {
        return service.getByCpfCnpj(cpfCnpj,tipo).getContactOpaque()
    }

    @GetMapping("contas-receber")
    fun getContasReceberByCliente(@RequestParam("CardCode") cardCode: String): ResponseEntity<NextLink<ContasReceberDto>> {
          val result = service.getContasReceberByCardCode(cardCode)
          return ResponseEntity.ok(result)
      }

    @PostMapping("/contas-receber/nextlink")
    fun nextLinkContasReceber(@RequestBody link: String, auth: Authentication): ResponseEntity<NextLink<ContasReceberDto>> {
        val result = service.next(link).tryGetNextValues<ContasReceberDto>()
        return ResponseEntity.ok(result)
    }

}
