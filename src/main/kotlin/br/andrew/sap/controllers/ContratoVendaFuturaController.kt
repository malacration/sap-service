package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.ItemTroca
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("contrato-venda-futura")
class ContratoVendaFuturaController(
    val service : ContratoVendaFuturaService,
    val pedidoService : OrdersService,
    val itemService: ItemsService,
    val comissaoService: ComissaoService,
    val adiantamentoService : DownPaymentService,
    val creditNoteService: CreditNotesService,
    val batchService: BatchService,
    @Value("\${venda-futura.entrega:9}") val utilizacaoEntregaVendaFutura : Int,
    val cotacaoController : QuotationsController){
    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        val contratos = service.get(Filter("U_vendedor",auth.getIdInt(),Condicao.EQUAL),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Contrato>(page)
        return ResponseEntity.ok(contratos)
    }

    @PostMapping("pedido-retirada")
    fun pedidoRetirada(@RequestBody pedidoRetirada : PedidoRetirada, auth : Authentication) : ResponseEntity<Document?> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val contrato = service.get(Filter(
            Predicate("DocEntry",pedidoRetirada.docEntryVendaFutura,Condicao.EQUAL)
        )).tryGetValues<Contrato>().firstOrNull() ?: throw  Exception("O contrato nao foi encontrado")
        val cotacao = pedidoRetirada.parse(contrato,utilizacaoEntregaVendaFutura)
        return ResponseEntity.ok(cotacaoController.saveForAngular(cotacao,auth))
    }

    //TODO adicionar uma condicao na autorizacao financeira que retirada (quando tem id contrato) deve ser autorizada
    // quando o cliente esta inadimplente

    //Criar esquema de transaction e rollback do SAP
    //FEITO!

    //ID contrato
    //ItemParaRemover | quantidade
    //ItemNovo | precoNegociado | tabela | valorTabela | desconto | quantidade

    //Carregar todas as entregas
    //Verificar se pode remover o item!

    @PostMapping("troca")
    fun troca(@RequestBody pedidoTroca : PedidoTroca, auth : Authentication): BigDecimal {
        //TODO troca somente sem inadiplencia??

        val bathcList = BatchList()
        val contrato = service
            .getById(pedidoTroca.docEntry)
            .tryGetValue<Contrato>()

        val entregas = this.pedidoService.getByContrato(contrato)
        if(entregas.size > 0)
            throw Exception("Existe entregas programadas para esse contrato, cancele elas para realizar a troca!")

        // TODO testar esse cenario?
        // So pode existir troca quando nao estiver tudo entregue. (deixar transaction notification lidar com isso)

        val resultado = contrato.troca(pedidoTroca,itemService,comissaoService)
        bathcList.add(BatchMethod.PUT,contrato,service)

        if(resultado.compareTo(BigDecimal.ZERO) > 0.0){
            val adiantamento = service.adiantamentoComplementarVendaFuturaWithoutSave(contrato,resultado)
            bathcList.add(BatchMethod.POST,adiantamento,adiantamentoService)
        }else if(resultado.compareTo(BigDecimal.ZERO) < 0.0){
            val resultadoModulo = resultado.multiply(BigDecimal(-1))
            val adiantamentoCancelar = service.adiantamentosAhCancelar(contrato,resultadoModulo)
            val valorResidual = adiantamentoCancelar.sumOf { BigDecimal(it.total()) }.minus(resultadoModulo)

            adiantamentoCancelar.forEach {
                bathcList.add(BatchMethod.POST,CreditNotes(it),creditNoteService)
            }

            if(valorResidual.compareTo(BigDecimal.ZERO) > 0){
                val dataVencimento = adiantamentoCancelar.map { it.calcularDataDeVencimento() }.minBy { it }
                val adiantamentoComplementar = adiantamentoService.adiantamentosVendaFuturaWithoutSave(contrato,
                    PaymentDueDates(valorResidual,dataVencimento.toLocalDate())
                )
                bathcList.add(BatchMethod.POST,adiantamentoComplementar,adiantamentoService)
            }
            //Cancelar boleto e emitir novos com o saldo remanecente
            //Se ja estiver tudo pago e o valor e inferior, eu nao cancelo nenhum boleto e o
            // financeiro tem que resolver a devolucao para o cliente!
        }

        //Criar um novo pedido de venda com os novos dados.
        //O item remanecente deve ter a sua linha copiada do pedido original e ter sua quanidade ajustada

        //Esse novo pedido de venda vai alterar o contrato atual?
        logger.info("Enviando ao sap o pedido de troca para o contrato ${contrato.getId()}")
        batchService.run(bathcList)
        return resultado
    }

    //Remover esse metodo depois
    @GetMapping("teste")
    fun testeTroca(auth : Authentication): BigDecimal {
        val itemSaida = listOf(ItemTroca("PAC0000145",50.0,93.5))
        val itemRecebido = listOf(
            Product("PAC0000044","45.0","10.0",0).also {
                it.ItemDescription = "OX SEMICONFINAMENTO"
                it.U_idTabela = 4
                it.DiscountPercent = 0.0
                it.U_preco_base = 104.0
                it.U_preco_negociado = 104.0
                it.MeasureUnit = "6"
                it.PriceList = 4
            }
        )
        return troca(PedidoTroca("68",itemSaida,itemRecebido),auth)
    }

    @GetMapping("teste2")
    fun testeTrocaRemocao(auth : Authentication): BigDecimal {
        val itemSaida = listOf(ItemTroca("PAC0000145",1.0,93.5))
        return troca(PedidoTroca("68",itemSaida, listOf()),auth)
    }
}
