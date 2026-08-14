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
    val ordersService: OrdersService
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

    fun criar(tipoDocumento: String, motivo: String, documento: Document, solicitante: String): Autorizacao {
        val autorizacao = Autorizacao(
            U_tipoDocumento = tipoDocumento,
            U_motivo = motivo,
            U_cardCode = documento.CardCode,
            U_payload = objectMapper.writeValueAsString(documento),
        ).also {
            it.U_cardName = documento.cardName
            it.U_valor = documento.total()
            it.U_solicitante = solicitante
        }
        return save(autorizacao).tryGetValue()
    }

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
