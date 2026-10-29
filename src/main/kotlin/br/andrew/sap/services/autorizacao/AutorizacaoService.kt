package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sistema.Autorizacao
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sistema.StatusAutorizacao
import br.andrew.sap.model.sistema.TipoDocumentoAutorizacao
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.documents.QuotationsService
import br.andrew.sap.services.security.AuthService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

//fila propria da aplicacao: um documento retido aqui nunca chega a ir pro SAP ate
//ser aprovado - na aprovacao, o payload guardado e reenviado pelo MESMO caminho de
//sempre (QuotationsService.save/OrdersService.save), o "fluxo direto"
@Service
class AutorizacaoService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val quotationsService: QuotationsService,
    val ordersService: OrdersService,
    val autorizadorService: AutorizadorService,
    private val sequencia: SequenciaAutorizacao,
) : EntitiesService<Autorizacao>(env, restTemplate, authService) {

    //mesmo mapper (com o KotlinModule configurado certo pros construtores de
    //Document/Quotation/OrderSales) que OData.tryGetValue ja usa pra desserializar
    //essas mesmas classes vindas do SAP - reaproveitado aqui pro round-trip do payload
    private val objectMapper = OData().mapper

    override fun path() = "/b1s/v1/autorizacao"

    fun getTodas(): List<Autorizacao> {
        return getAll(Autorizacao::class.java)
    }

    fun get(id: Int): Autorizacao {
        return getById(id).tryGetValue()
    }

    /**
     * Retem o documento para aprovacao. Motivo sem autorizador cadastrado NAO retem: o documento
     * ficaria pendente sem ninguem que pudesse decidir. Falha com a explicacao e o documento
     * nao e criado - quem vende sabe o motivo e quem administra sabe o que cadastrar.
     */
    fun criar(
        tipoDocumento: String,
        motivo: String,
        documento: Document,
        solicitante: String,
        offlineId: String? = null
    ): Autorizacao {
        if (autorizadorService.getTodos().none { it.U_motivo == motivo })
            throw Exception("Este documento precisa de autorização pelo motivo $motivo, mas não há " +
                "nenhum autorizador cadastrado para esse motivo. Peça ao administrador para cadastrar " +
                "um autorizador ou desligar a regra nas filiais.")

        val autorizacao = Autorizacao(
            U_tipoDocumento = tipoDocumento,
            U_motivo = motivo,
            U_cardCode = documento.CardCode,
            U_payload = objectMapper.writeValueAsString(documento),
        ).also {
            it.U_cardName = documento.cardName
            it.U_valor = documento.total()
            it.U_solicitante = solicitante
            it.U_offline_id = offlineId
            //@AUTORIZACAO e bott_MasterData: o service layer recusa POST sem codigo
            //("Enter valid code [@AUTORIZACAO.Code]") mesmo com ManageSeries no UDO
            it.Code = proximoCodigo()
            it.Name = it.Code.toString()
        }
        return save(autorizacao).tryGetValue()
    }

    //Redis (ou contador local) semeado com o maior Code do SAP no startup - ver SequenciaAutorizacao
    fun proximoCodigo(): Int = sequencia.proximo(::maiorCodigoNoSap)

    /**
     * Le so o Code ($select): a lista inteira traz o U_payload (o documento serializado) de cada
     * autorizacao ja feita. Ordenar pelo SAP nao serve: o Code e texto e "9" viria antes de "10".
     */
    fun maiorCodigoNoSap(): Int {
        var odata = codigos()
        val existentes = odata.tryGetValues<CodigoAutorizacao>().toMutableList()
        while (odata.hasNext()) {
            odata = next(odata)
            existentes.addAll(odata.tryGetValues<CodigoAutorizacao>())
        }
        return existentes.mapNotNull { it.Code?.toIntOrNull() }.maxOrNull() ?: 0
    }

    //aberto para o teste: busca so a coluna Code de todas as autorizacoes
    internal fun codigos(): OData =
        exchangeWithValidSession(OData::class.java) { session ->
            RequestEntity
                .get(env.host + path() + "?\$select=Code")
                .header("cookie", session.cookieHeader())
                .build()
        }.body ?: OData()

    fun aprovar(id: Int, usuario: String): Autorizacao {
        val autorizacao = get(id)
        if (autorizacao.U_status != StatusAutorizacao.PENDENTE)
            throw Exception("Essa autorizacao ja foi decidida (status atual: ${autorizacao.U_status})")

        val documentoCriado = when (autorizacao.U_tipoDocumento) {
            TipoDocumentoAutorizacao.COTACAO ->
                quotationsService.save(objectMapper.readValue(autorizacao.U_payload, Quotation::class.java))
                    .tryGetValue<Document>()
            TipoDocumentoAutorizacao.PEDIDO_VENDA ->
                ordersService.save(objectMapper.readValue(autorizacao.U_payload, OrderSales::class.java))
                    .tryGetValue<Document>()
            else -> throw Exception("Tipo de documento desconhecido: ${autorizacao.U_tipoDocumento}")
        }

        autorizacao.U_status = StatusAutorizacao.APROVADO
        autorizacao.U_autorizador = usuario
        autorizacao.U_docEntryCriado = documentoCriado.docEntry
        update(autorizacao, "'$id'")
        return get(id)
    }

    fun rejeitar(id: Int, usuario: String, observacao: String?): Autorizacao {
        val autorizacao = get(id)
        if (autorizacao.U_status != StatusAutorizacao.PENDENTE)
            throw Exception("Essa autorizacao ja foi decidida (status atual: ${autorizacao.U_status})")

        autorizacao.U_status = StatusAutorizacao.REJEITADO
        autorizacao.U_autorizador = usuario
        autorizacao.U_observacao = observacao
        update(autorizacao, "'$id'")
        return get(id)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class CodigoAutorizacao(@JsonProperty("Code") val Code: String? = null)
