package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.logistica.Carregamento
import br.andrew.sap.model.logistica.PedidoUpdate
import br.andrew.sap.model.logistica.PedidoUpdateLine
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.sap.SequenceCode
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.OrdersService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CarregamentoService(val batchService: BatchService, val pedidoVendaService : OrdersService, val sqlQueriesService : SqlQueriesService, env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
    EntitiesService<Carregamento>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ORD_CARREGAMENTO"
    }

    fun getEstoqueEmCarregamento(ItemCode: String): Int {
        val parameters = listOf(
            Parameter("ItemCode", ItemCode)
        )
        return sqlQueriesService
            .execute("estoque-em-ordem.sql", parameters)
            ?.tryGetValues<Map<String, Any>>()
            ?.firstOrNull()
            ?.get("total_quantidade") as Int? ?: 0
    }

    fun docEntryPedido(idOrdemCarregamento: Int): List<DocEntry> {
        val parameters = listOf(
            Parameter("U_ORD_CARREGAMENTO", idOrdemCarregamento)
        )
        return sqlQueriesService
            .getAll<DocEntry>("docentry-ordem-carregamento.sql", parameters)
    }

    fun cancelarPedidos(idOrdem: Int, docNums: List<Int>) {
        docNums.forEach { docNum ->
            val parameters = listOf(Parameter("DocNum", docNum.toString()))
            sqlQueriesService.execute("cancelPedido.sql", parameters)
        }
    }


    fun getTotaisPedidoseQuantidade(ordemCarregamento: Int): List<Carregamento> {
        val parameters = listOf(
            Parameter("ordCarregamento", ordemCarregamento) // igual ao do SQL
        )
        return sqlQueriesService
            .execute("pedidoeestoque.sql", parameters)
            ?.tryGetValues<Carregamento>()
            ?: emptyList()
    }

    fun procuraSequenceCode(BPLId: String): List<SequenceCode> {
        val parameters = listOf(Parameter("BPLId", BPLId))

        return sqlQueriesService
            .execute("procura-sequence-code.sql", parameters)
            ?.tryGetValues<SequenceCode>()
            ?: emptyList()
    }

    fun cancelar(id: String): Carregamento {
        val docEntry = id.toInt()

        val pedidosVinculados = docEntryPedido(docEntry)

        val tripleRemover = pedidosVinculados.map { ref ->
            val order = pedidoVendaService.getById(ref.DocEntry.toString()).tryGetValue<OrderSales>()

            val pedidosUpdate = PedidoUpdate(
                order.DocEntry.toString(),
                order.DocumentLines.map { PedidoUpdateLine(it.DocEntry!!, it.LineNum!!, null) }
            )
            Triple(BatchMethod.PATCH, pedidosUpdate, pedidoVendaService)
        }

        if (tripleRemover.isNotEmpty()) {
            batchService.run(BatchList().addAll(tripleRemover))
        }

        val carregamento = getById(id).tryGetValue<Carregamento>()
        carregamento.U_Status = "Cancelado"

        update(carregamento, id)

        return carregamento
    }
}