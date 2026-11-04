package br.andrew.sap.model.logistica

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CarregamentoDto(
        val ordemCarregamento: Carregamento,
        val pedidos : List<Int>,
        val pedidosRemover : List<Int> = listOf(),
        //null = front antigo, nao mexe nas retiradas da ordem; lista vazia = remove todas.
        val faturasReserva : List<RetiradaFatura>? = null
)

/** Quanto retirar de uma linha de fatura de reserva (OINV isIns = 'Y') nesta ordem. */
data class RetiradaFatura(val docEntry : Int, val lineNum : Int, val quantidade : Double)


class PedidoLinha(val docEntry : Int, val lineNum : Int){

        fun criaPedidoUpdate(orderId : Int): PedidoUpdateLine {
                return PedidoUpdateLine(docEntry,lineNum,orderId.toString())
        }
}
