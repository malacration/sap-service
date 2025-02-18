package br.andrew.sap.services.calculadora

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.Receita
import br.andrew.sap.model.calculadora.ReceitaLinhas
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.pricing.PriceListsService
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.stock.ResourceService
import br.andrew.sap.services.stock.UnitOfMeasurementGroupsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CalculadoraHanddleService(
    val itemService: ItemsService,
    val productTreesService: ProductTreesService,
    val priceListService : PriceListsService,
    @Value("\${idUnitKg:3}") val idKg : Int,
    val resourceService : ResourceService,
    val unitGroupsService: UnitOfMeasurementGroupsService
) {

    fun getBasicIngredientes(itemCode: String, itensComEstrutura: List<String>,qtdMutiplo : BigDecimal, warehouse: String): List<Produto> {
        val receita = try {
            productTreesService.getById(itemCode).tryGetValue<Receita>()
        }catch (e : Exception){
            return listOf()
        }

        val linhasItemReceita : List<ReceitaLinhas>  = receita.ProductTreeLines
            .filter { !itensComEstrutura.contains(it.ItemCode) }
        val materiasPrimas : List<Produto> = if(linhasItemReceita.isEmpty()) listOf() else itemService
            .getAll(Produto::class.java, Filter("ItemCode", linhasItemReceita.map { it.ItemCode }, Condicao.IN))
            .also {
                it.forEach {
                    it.defaultWareHouse = linhasItemReceita
                        .filter { receitaItem -> receitaItem.ItemCode == it.ItemCode }
                        .firstOrNull()?.Warehouse ?: warehouse
                    it.pai = itemCode
                }
            }

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
            .getByIdCacheable(produto.UoMGroupEntry ?: throw Exception("o UoMGroupEntry no item ${produto.ItemCode} esta nulo"))
        return grupoUnidade.convertQuantity(produto.InventoryUoMEntry ?: 0,this.idKg,1.0)
    }
}