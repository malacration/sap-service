package br.andrew.sap.model.sap

import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BatchesGroupByItemCode {

    var ItemCode : String? = null
    var Batches: List<BatchStock>? = null

    fun getBachesBy(line : DocumentLines) : List<BatchStock> {
        if(line.ItemCode != ItemCode)
            throw Exception("Nao é possivel pegar lotes para produtos distintos $ItemCode com ${line.ItemCode}")
        if(Batches == null)
            throw Exception("Nao existe nenhum lote no agrupador de lotes")

        val qtdLine = BigDecimal(line.Quantity)
        val resultado = mutableListOf<BatchStock>()

        Batches?.forEach { batch ->
            val totalSelecionado = resultado.sumOf { BigDecimal(it.Quantity) }
            if(totalSelecionado != BigDecimal(line.Quantity)){
                resultado.add(batch.getAmount(line, qtdLine.minus(totalSelecionado)))
            }
        }
        return resultado
    }
}
