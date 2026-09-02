package br.andrew.sap.services.cadastro
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.toInt
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.dto.ContasReceberDto
import br.andrew.sap.model.dto.TituloVencidoDto
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.partner.*
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import br.andrew.sap.services.security.AuthService

@Service
class BusinessPartnersService(
    val sqlQueriesService : SqlQueriesService,
    env: SapEnvrioment,
    restTemplate: RestTemplate,
     authService: AuthService) :
        EntitiesService<BusinessPartner>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/BusinessPartners"
    }

    fun addPaymentMethod(cardCode: String, idFormaPagamento: String): OData? {
        val bp: BusinessPartner = BusinessPartner().also {
            it.setBPPaymentMethods(listOf(PaymentMethod(idFormaPagamento)))
        }
        return update(bp, "'${cardCode}'")
    }

    fun addBusinesPlace(cardCode: String, idBusinesPlace: String): OData? {
        val bp: BusinessPartner = BusinessPartner().also {
            it.bpBranchAssignment = listOf(BPBranchAssignment().also {
                it.BPCode = cardCode
                it.bplid = idBusinesPlace
                it.DisabledForBP = Cancelled.tNO
            })
        }
        return update(bp, "'${bp.cardCode}'")
    }

    /**
     * O service layer substitui a colecao BPAddresses inteira no patch, por isso
     * lemos o parceiro, trocamos a localidade do endereco e reenviamos todos.
     *
     * A chave de endereco no SAP e (AddressName, AddressType), NAO so o nome: o mesmo nome pode
     * existir nos dois tipos. Casar so pelo nome pegava o primeiro da lista - cliente com um
     * "COBRANCA" de cobranca e outro "COBRANCA" de entrega recebia a localidade no endereco
     * errado, sempre no de cobranca.
     */
    fun atualizaLocalidadeEndereco(cardCode: String, addressName: String, addressType: AddresType,
                                   localidade: Int?): BusinessPartner {
        val bp = getById("'$cardCode'").tryGetValue<BusinessPartner>()
        val enderecos = bp.getAddresses()
        val endereco = enderecos.firstOrNull { it.addressName == addressName && it.addressType == addressType }
            ?: throw Exception("Endereco $addressName do tipo ${addressType.cardType} nao encontrado no parceiro $cardCode")
        endereco.U_Localidade = localidade
        update(BusinessPartner().also { it.setAddresses(enderecos) }, "'$cardCode'")
        return getById("'$cardCode'").tryGetValue()
    }

    fun atualizaKey(key: String, bp: BusinessPartner): OData? {
        return update(
            "{" +
                    " \"U_keyUpdate\" : \"$key\", " +
                    " \"U_fazer_fluxo_prazo\" : 0 " +
                    "}", "'${bp.cardCode}'"
        )
    }

    fun atualizaDataSerasa(date: Date, bp: BusinessPartner): OData? {
        val strDate = SimpleDateFormat("yyyy-MM-dd").format(date)
        return update(
            "{" +
                    " \"U_dataSerasa\" : \"$strDate\" " +
                    "}", "'${bp.cardCode}'"
        )
    }

    fun getByCpfCnpj(cpfCnpj: String, type: BusinessPartnerType): BusinessPartner {
        val url = env.host + "/b1s/v1/"
        var uri =
            "${url}SQLQueries('parceiro.sql')/List?valor='${CpfCnpj(cpfCnpj).getWithMask()}'&type='${type.getForSql()}'"
        val request = RequestEntity
            .get(uri)
            .header("cookie", session().cookieHeader())
            .build()
        val odata = restT.exchange(request, OData::class.java)
            .body?.tryGetValues<BusinessPartner>()
            ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")
        return getById(
            "'${odata.firstOrNull()?.cardCode ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")}'"
        )
            .tryGetValue<BusinessPartner>()
    }

    //TODO melhorar depois
    fun fullSearchText(fullText: String, auth: Authentication): NextLink<BusinessPartnerSlin> {
        val request = RequestEntity
            .get(env.host + "/b1s/v1/sml.svc/CLIENTE_VENDEDORParameters(searchText='$fullText',vendedor=${auth.principal})/CLIENTE_VENDEDOR")
            .header("cookie", session().cookieHeader())
            .build()
        return restT.exchange(request, OData::class.java).body!!.tryGetNextValues()
    }

    fun fullSearchTextFallBack(fullText: String, user: User): NextLink<BusinessPartnerSlin> {
        if (fullText.startsWith("SQLQueries('parceiro-full-search-text.sql')"))
            return sqlQueriesService.nextLink(fullText)!!.tryGetNextValues()
        //Termo em maiusculo: o HANA e case sensitive no LIKE, e o normalizador deixa CardName
        //todo em caixa alta - buscar "mauro" nao acharia "MAURO CARRETA". CardCode e TaxId, os
        //outros campos que a view casa, ja sao maiusculos/numericos, entao nada se perde.
        //O ramo de CPF/CNPJ e so digito e nao passa por aqui.
        val busca =
            if (fullText.toDoubleOrNull() == null) fullText.replace("*", "%").uppercase(Locale.ROOT) else CpfCnpj(fullText).getWithMask();
        val parametros = listOf(
            Parameter("superVendedor", user.superVendedor()),
            Parameter("valor", "'%${busca}%'"),
            Parameter("vendedor", user.principal)
        )
        return sqlQueriesService
            .execute("parceiro-full-search-text.sql", parametros)!!
            .tryGetNextValues()
    }

    fun searchBusinessPartners(search: String): List<BusinessPartnerSlin> {
        if (search.isBlank()) {
            throw IllegalArgumentException("O parâmetro de busca não pode estar vazio.")
        }

        //mesma razao do fullSearchTextFallBack: LIKE case sensitive contra nome ja normalizado
        val busca = if (search.toDoubleOrNull() == null) search.replace("*", "%").uppercase(Locale.ROOT) else CpfCnpj(search).getWithMask()
        val parametros = listOf(
            Parameter("valor", "'%$busca%'")
        )

        val result: NextLink<BusinessPartnerSlin> = sqlQueriesService
            .execute("parceiro-limitado-cpf.sql", parametros)!!
            .tryGetNextValues()

        return result.content
    }

    fun normalizeAddressName(bp: BusinessPartner) {
        bp.getAddresses().forEach {
            it.addressName = it.normalize()
        }
        val novo = BusinessPartner().also {
            it.setAddresses(bp.getAddresses())
        }
        update(novo, "'${bp.cardCode}'")

    }

    fun findAllBySalePerson(idSalesPerson: Int, page: Pageable): Page<BusinessPartner> {
        val filter = Filter((mutableListOf(Predicate("SalesPersonCode", idSalesPerson, Condicao.EQUAL))))
        return get(filter).tryGetPageValues<BusinessPartner>(page)
    }

    fun modificarVendedor(businessPartners: BusinessPartner, idSalesPerson: Int): BusinessPartner {
        return businessPartners.also {
            val json =
                "{ \n" +
                        "                    \"SalesPersonCode\" : \"${idSalesPerson}\"\n" +
                        "                }"
            this.update(json, "'${it.cardCode}'")
        }
    }

    fun findBusinessPartnersBySalesPersonCode(salesPersonCode: Int, page: Pageable): Page<BusinessPartner>? {
        val filter = Filter(
            Predicate("SalesPersonCode", salesPersonCode, Condicao.EQUAL)
        )
        val result = get(filter, page)
        return result.tryGetPageValues<BusinessPartner>(page)
    }

    fun getContasReceberByCardCode(cardCode: String): NextLink<ContasReceberDto> {
        if (cardCode.isBlank()) {
            throw IllegalArgumentException("O parâmetro 'cardCode' não pode estar vazio.")
        }

        val parametros = listOf(
            Parameter("cardCode", "'$cardCode'")
        )

        val result: NextLink<ContasReceberDto> = sqlQueriesService
            .execute("contas-receber.sql", parametros)!!
            .tryGetNextValues()

        return result
    }

    //cliente com titulo vencido ha mais de 3 dias e nao reconciliado (mesma regra
    //de ClienteInadimplentesByContabilidade do sap-sql) - usado pelo motor de
    //regras de autorizacao (ClienteEmAtrasoRegra)
    fun temTituloVencido(cardCode: String): Boolean {
        if (cardCode.isBlank())
            return false

        //SQLQueries do service layer nao suporta ADD_DAYS/NOW() (erro 701 "Cannot
        //support this function or expression") - a data limite e calculada aqui e
        //passada como parametro, mesmo padrao ja usado em ParcelasAbertoService/titulos.sql
        val dataLimite = LocalDate.now().minusDays(3)
        val parametros = listOf(
            Parameter("cardCode", "'$cardCode'"),
            Parameter("dataLimite", "'$dataLimite'")
        )

        val result: NextLink<TituloVencidoDto> = sqlQueriesService
            .execute("cliente-em-atraso.sql", parametros)!!
            .tryGetNextValues()

        return result.content.isNotEmpty()
    }
}