package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.Receita
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.stock.UnitOfMeasurementGroups
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.stock.UnitOfMeasurementGroupsService
import br.andrew.sap.services.pricing.PriceListsService
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("calculadora-preco")
class CalculadoraPrecoController(
    val itemService: ItemsService,
    val productTreesService: ProductTreesService,
    val priceListService : PriceListsService,
    val unitGroupsService: UnitOfMeasurementGroupsService
) {

    @GetMapping("/itens/all")
    fun get(
        @RequestParam(name = "itemCodePrefix", defaultValue = "PAC0000259") itemCodePrefix: String,
        @RequestParam(name = "warehouse") warehouse: String,
        page : Pageable,
        auth : Authentication): List<Produto> {
        val itensComEstrutura = itemService.produtosComEstrutura().map { it.ItemCode }
        val filter = Filter(
            Predicate("ItemCode",itemCodePrefix, Condicao.STARTS_WITH),
            Predicate("Valid", YesNo.tYES, Condicao.EQUAL)
        )

        //Mudar para get all
        return itemService.get(filter).tryGetValues<Produto>()
            .filter { itensComEstrutura.contains(it.ItemCode) }
            .parallelStream()
            .toList().also {
                it.forEach{
                    it.defaultWareHouse = warehouse
                    it.kgsPorUnidade = try { getKgsPorUnidade(it) }catch (e : Exception) { BigDecimal(1.0) }
                    it.custoGgf = BigDecimal(3.5)
                    it.ingredientes = getBasicIngredientes(it.ItemCode,itensComEstrutura,BigDecimal("1.0"),warehouse)
            }}
    }
    fun getBasicIngredientes(itemCode: String, itensComEstrutura: List<String>,qtdMutiplo : BigDecimal, warehouse: String): List<Produto> {
        val receita = try {
             productTreesService.getById(itemCode).tryGetValue<Receita>()
        }catch (e : Exception){
            return listOf()
        }

        val materiasPrimas : List<Produto>  = receita.ProductTreeLines
            .filter { !itensComEstrutura.contains(it.ItemCode) }
            .flatMap { itemReceita -> itemService.getAll<Produto>(Filter("ItemCode", itemReceita.ItemCode,Condicao.IN)).also {
                it.forEach {
                    it.defaultWareHouse = itemReceita.Warehouse
                    it.pai = itemCode
                }
            } }

        val itensRecurcao : List<Produto> = receita.ProductTreeLines
            .filter { itensComEstrutura.contains(it.ItemCode) }
            .flatMap { getBasicIngredientes(it.ItemCode, itensComEstrutura, it.Quantity*qtdMutiplo , warehouse) }

        return itensRecurcao + materiasPrimas
            .filter { !itensComEstrutura.contains(it.ItemCode) }
            .also {
                it.forEach {it.QuantidadeNaReceita = qtdMutiplo.multiply(receita.qtdMaterial(it.ItemCode)) }
            }
    }

    fun getKgsPorUnidade(produto : Produto) : BigDecimal{
        val grupoUnidade = unitGroupsService
            .getById(produto.UoMGroupEntry ?: throw Exception("o UoMGroupEntry no item ${produto.ItemCode} esta nulo"))
            .tryGetValue<UnitOfMeasurementGroups>()
        //TODO remover magic number
        var magicNumber = 4
        return grupoUnidade.UoMGroupDefinitionCollection
            .find { it.AlternateUoM == magicNumber }
            ?.AlternateQuantity?.toBigDecimal() ?: throw Exception("AlternateQuantity is nulo para o ${produto.ItemCode}")
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