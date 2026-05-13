package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.dto.PedidoTesteRequest
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.AddressExtension
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.stock.ItemsService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

@Service
class PedidoTesteService(
    private val ordersService: OrdersService,
    private val sqlQueriesService: SqlQueriesService,
    private val itemService: ItemsService
) {

    fun criarPedidosTeste(request: PedidoTesteRequest): List<Document> {
        require(request.quantidade > 0) { "A quantidade deve ser maior que zero" }
        require(request.localidade.isNotBlank()) { "A localidade deve ser informada" }

        val pedidosModelo = buscarPedidosModelo(request)
        if (pedidosModelo.isEmpty()) {
            throw Exception("Nenhum pedido modelo encontrado para a localidade ${request.localidade}")
        }

        val docEntriesSelecionados = List(request.quantidade) { pedidosModelo.random() }
        return docEntriesSelecionados.mapIndexed { index, docEntry ->
            val modelo = ordersService.getById(docEntry.toString()).tryGetValue<OrderSales>()
            val pedidoTeste = criarPedidoAPartirDoModelo(modelo, request.localidade, index)
            ordersService.save(pedidoTeste).tryGetValue<Document>()
        }
    }

    private fun buscarPedidosModelo(request: PedidoTesteRequest): List<Int> {
        val inicioPadrao = LocalDate.now().minusMonths(6).format(DateTimeFormatter.ISO_DATE)
        val fimPadrao = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val parameters = listOf(
            Parameter("localidade", request.localidade),
            Parameter("filial", request.filial ?: -1),
            Parameter("filialIsFilter", if (request.filial == null) Int.MAX_VALUE else -1),
            Parameter("dataInicial", request.dataInicial ?: inicioPadrao),
            Parameter("dataFinal", request.dataFinal ?: fimPadrao),
        )

        return sqlQueriesService
            .getAll<DocEntry>("pedidos-modelo.sql", parameters)
            .mapNotNull { it.DocEntry }
    }

    private fun criarPedidoAPartirDoModelo(modelo: OrderSales, localidade: String, indice: Int): OrderSales {
        val linhasModelo = modelo.DocumentLines.filterIsInstance<Product>()
        if (linhasModelo.isEmpty()) {
            throw Exception("Pedido modelo ${modelo.docEntry} sem linhas de produto para gerar teste")
        }

        val quantidadeLinhas = Random.nextInt(1, minOf(3, linhasModelo.size) + 1)
        val linhasSelecionadas = linhasModelo.shuffled().take(quantidadeLinhas).map { clonarLinhaComVariacao(it) }
        val dataEntrega = LocalDate.now().plusDays(Random.nextLong(1, 8)).format(DateTimeFormatter.ISO_DATE)

        return OrderSales(
            modelo.CardCode,
            dataEntrega,
            linhasSelecionadas,
            modelo.getBPL_IDAssignedToInvoice()
        ).also {
            it.paymentMethod = modelo.paymentMethod
            it.paymentGroupCode = modelo.paymentGroupCode
            it.salesPersonCode = modelo.salesPersonCode
            it.comments = "Pedido de teste gerado automaticamente a partir do modelo ${modelo.docNum ?: modelo.docEntry}"
            it.OpeningRemarks = it.comments
            it.controlAccount = modelo.controlAccount
            it.model = modelo.model
            it.shipToCode = modelo.shipToCode
            it.AddressExtension = AddressExtension().also { address ->
                address.U_LocalidadeS = localidade
                address.U_LocalidadeB = modelo.AddressExtension?.U_LocalidadeB ?: localidade
            }
            it.Address = modelo.Address
            it.Address2 = modelo.Address2
            it.documentAdditionalExpenses = modelo.documentAdditionalExpenses.toMutableList()
            it.journalMemo = modelo.journalMemo
            it.u_pedido_update = "1"
            it.u_id_pedido_forca = "TESTE-${System.currentTimeMillis()}-$indice"
            it.u_uuid_forca = UUID.randomUUID().toString()
            it.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
            it.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
            it.atualizaPrecoBase(itemService)
        }
    }

    private fun clonarLinhaComVariacao(modelo: Product): Product {
        val quantidade = variarQuantidade(modelo.Quantity)
        val unitPrice = variarPreco(modelo.UnitPrice)

        return Product(modelo.ItemCode ?: throw Exception("Linha sem ItemCode"), quantidade, unitPrice, modelo.Usage).also {
            copiarCampos(modelo, it)
            it.LineTotal = BigDecimal(quantidade)
                .multiply(BigDecimal(unitPrice))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }
    }

    private fun copiarCampos(origem: DocumentLines, destino: Product) {
        destino.ItemDescription = origem.ItemDescription
        destino.CommisionPercent = origem.CommisionPercent
        destino.U_idTabela = origem.U_idTabela
        destino.TaxCode = origem.TaxCode
        destino.DiscountPercent = origem.DiscountPercent
        destino.U_preco_base = origem.U_preco_base
        destino.U_preco_negociado = origem.U_preco_negociado
        destino.WarehouseCode = origem.WarehouseCode
        destino.U_id_item_forca = origem.U_id_item_forca
        destino.CostingCode = origem.CostingCode
        destino.CostingCode2 = origem.CostingCode2
        destino.AccountCode = origem.AccountCode
        destino.MeasureUnit = origem.MeasureUnit
        destino.PriceList = origem.PriceList
        destino.PriceUnit = origem.PriceUnit
        destino.U_LBR_Destinacao = origem.U_LBR_Destinacao
        destino.CFOPCode = origem.CFOPCode
    }

    private fun variarQuantidade(quantidadeOriginal: String): String {
        val quantidade = BigDecimal(quantidadeOriginal)
        val fator = BigDecimal(Random.nextDouble(0.7, 1.3).toString())
        val novaQuantidade = quantidade.multiply(fator).setScale(0, RoundingMode.HALF_UP)
        return if (novaQuantidade.compareTo(BigDecimal.ZERO) <= 0) {
            "1"
        } else {
            novaQuantidade.toPlainString()
        }
    }

    private fun variarPreco(precoOriginal: String): String {
        val preco = BigDecimal(precoOriginal)
        val fator = BigDecimal(Random.nextDouble(0.9, 1.1).toString())
        val novoPreco = preco.multiply(fator).setScale(4, RoundingMode.HALF_UP)
        return if (novoPreco.compareTo(BigDecimal("0.0001")) <= 0) {
            BigDecimal("0.0001").toPlainString()
        } else {
            novoPreco.stripTrailingZeros().toPlainString()
        }
    }
}
