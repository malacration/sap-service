package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.calculadora.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.calculadora.CalculadoraHanddleService
import br.andrew.sap.services.stock.ResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("calculadora-preco")
class CalculadoraPrecoController(
    val itemService: ItemsService,
    val calculadoraHandle : CalculadoraHanddleService,
    val productTreesService: ProductTreesService,
    val resourceService : ResourceService,
    @Value("\${ggf.code:GGF00001}") val ggfId : String,
) {

    @GetMapping("/itens/all")
    fun get(
        @RequestParam(name = "itemCodeStart") start: String,
        @RequestParam(name = "itemCodeEnd") end: String,
        @RequestParam(name = "warehouse") warehouse: String,
        page : Pageable,
        auth : Authentication): List<Produto> {

        val ggf = resourceService.getById(ggfId).tryGetValue<Resource>()
        var itensComEstrutura = itemService.produtosComEstrutura("").map { it.ItemCode }

        val filter = Filter(
            Predicate("ItemCode",start, Condicao.GREAT_EQUAL),
            Predicate("ItemCode",end, Condicao.LESS_EQUAL),
            Predicate("Valid", YesNo.tYES, Condicao.EQUAL)
        )

        return itemService.getAll(Produto::class.java,filter)
            .filter { itensComEstrutura.contains(it.ItemCode) }
            .also {
                it.forEach{
                    it.defaultWareHouse = warehouse
                    it.kgsPorUnidade = try { calculadoraHandle.getKgsPorUnidade(it) }catch (e : Exception) { BigDecimal(1.0) }
                    it.custoGgf = ggf.sumCost()
                    it.ingredientes = calculadoraHandle.getBasicIngredientes(it.ItemCode,itensComEstrutura,BigDecimal("1.0"),warehouse)
            }}
    }

    @GetMapping("/product-trees-service/all")
    fun productTree(
        @RequestParam(name = "itemCodePrefix", defaultValue = "PAC") itemCodePrefix: String,
        page : Pageable,
        auth : Authentication): OData {
        val filter = Filter(
            Predicate("TreeCode",itemCodePrefix, Condicao.STARTS_WITH)
        )
        return productTreesService.get(filter)
    }

}