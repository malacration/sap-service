package br.andrew.sap.controllers.producao

import JournalEntry
import br.andrew.sap.controllers.BranchController
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.calculadora.Receita
import br.andrew.sap.model.producao.Reprocessamento
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.inventory.InventoryGenEntries
import br.andrew.sap.services.inventory.InventoryGenExits
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.stock.ItemsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@RestController()
@RequestMapping("reprocessamento")
class ReprocessamentoController(
    val entradaEstoqueService: InventoryGenEntries,
    val saidaEstoqueService: InventoryGenExits,
    val journalEntriesService: JournalEntriesService,
    val itemService: ItemsService,
    val productTreesService: ProductTreesService,
    @Value("\${reprocessamento.account:1.1.3.003.00999}") val accountCode : String,
    @Value("\${reprocessamento.allow.prefix:PAC}") val prefixItemCode : String,

) {

    val logger = LoggerFactory.getLogger(BranchController::class.java)

    @GetMapping("entrada")
    fun getEntrada(): OData {
        return entradaEstoqueService.get()
    }

    @GetMapping("saida")
    fun getSaida(): OData {
        return saidaEstoqueService.get()
    }


    @GetMapping("entrada/{id}")
    fun getEntradaByid(@PathVariable id : String): OData {
        return entradaEstoqueService.getById(id)
    }

    @GetMapping("saida/{id}")
    fun getSaidaByid(@PathVariable id : String): OData {
        return saidaEstoqueService.getById(id)
    }

    @PostMapping()
    fun reprocessar(@RequestBody reprocessamento : Reprocessamento): Any? {
        if(!reprocessamento.itemCode.startsWith(prefixItemCode))
            throw Exception("Nao e permitido fazer reprocessamento de itens distintos de ${prefixItemCode}")
        val saida = saidaEstoqueService.save(reprocessamento.getSaida(accountCode))
        val journalSaidaId = saida.get("TransNum")
        try {
            val lc = journalEntriesService.getById(journalSaidaId?.toString() ?: "0").tryGetValue<JournalEntry>()
            val valorTotal = lc.journalEntryLines.sumOf { it.Credit }
            reprocessamento.itemCode
            val receita  = productTreesService.getById(reprocessamento.itemCode).tryGetValue<Receita>()
            val receitaLinha = receita.ProductTreeLines.find { it.ItemCode == reprocessamento.itemCodeTarget }
                ?: throw Exception("O ${reprocessamento.itemCodeTarget} nao existe na receita do ${reprocessamento.itemCodeTarget}")
            val qtdEntrada = receitaLinha.Quantity.divide(BigDecimal(receita.quantity)).multiply(BigDecimal(reprocessamento.quantidade))
            return entradaEstoqueService.save(reprocessamento.getEntrada(accountCode,qtdEntrada, valorTotal, receitaLinha.Warehouse))
        }catch (e : Exception){
            val lc = journalEntriesService.getById(journalSaidaId?.toString() ?: "0").tryGetValue<JournalEntry>()
            entradaEstoqueService.save(reprocessamento.getReverseSaida(accountCode,lc.journalEntryLines.sumOf { it.Credit }))
            throw e
        }
    }
}

