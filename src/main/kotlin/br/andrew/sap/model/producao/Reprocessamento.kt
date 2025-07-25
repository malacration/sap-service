package br.andrew.sap.model.producao

import br.andrew.sap.model.estoque.EntradaSaidaEstoque
import br.andrew.sap.model.estoque.Produto
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Reprocessamento(
    val itemCode: String,
    val quantidade: String,
    val deposito: String,
    val itemCodeTarget: String,
    val lotes: List<BatchStock>,
    //TODO ajustar depois, esse valor deve vir do frontend
    val BPLId : String = "2"
) {
    val id: String = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET,5)



    fun getSaida(accountCode: String): EntradaSaidaEstoque {
        return EntradaSaidaEstoque(
            BPLId,
            Produto(itemCode,quantidade,deposito)
                .also {
                    it.BatchNumbers = lotes.map { BatchStock(it.BatchNumber,it.Quantity) }
                    it.AccountCode = accountCode
                },
            "Saida Reprocessamento de Produtos ${id}"
        )
    }

    fun getReverseSaida(accountCode: String, valorTotal: Double) : EntradaSaidaEstoque {
        return EntradaSaidaEstoque(
            BPLId,
            Produto(itemCode,quantidade,deposito,BigDecimal(valorTotal).divide(BigDecimal(quantidade)).toString())
                .also {
                    it.BatchNumbers = lotes.map { BatchStock(it.BatchNumber,it.Quantity) }
                    it.AccountCode = accountCode
                },
            "Estorno Reprocessamento de Produtos ${id}"
        )
    }

    @Deprecated("Remover lote hardcode")
    fun getEntrada(accountCode: String, qtdEntrada: BigDecimal, valorTotal: Double, warehouse: String): EntradaSaidaEstoque {
        val valorUnitario = BigDecimal(valorTotal).divide(qtdEntrada,4, RoundingMode.HALF_DOWN).toString()
        return EntradaSaidaEstoque(
            BPLId,
            Produto(itemCodeTarget,
                qtdEntrada.toString(),
                warehouse,
                valorUnitario
                )
                .also {
                    it.BatchNumbers = listOf(BatchStock("repro",qtdEntrada.toString()))
                    it.AccountCode = accountCode
                },
            "Entrada Reprocessamento de Produtos ${id}"
        )
    }

}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BatchStock(
    val BatchNumber: String,
    val Quantity: String,
    val whsCode: String? = null,
    val ItemCode: String? = null,
    val expDate: String? = null,
    val inDate: String? = null,
    val itemName: String? = null,
    val mnfDate: String? = null
){

}