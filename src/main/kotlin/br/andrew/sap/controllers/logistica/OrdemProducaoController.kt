package br.andrew.sap.controllers.logistica

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.producao.OrdemProducao
import br.andrew.sap.model.producao.OrdemProducaoDetalhe
import br.andrew.sap.model.producao.ProdutoDaOrdem
import br.andrew.sap.model.producao.StatusOrdemProducao
import br.andrew.sap.services.logistica.ProductionOrdersService
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.comercial.ProductTreesService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ordens-producao")
class OrdemProducaoController(
    val service: ProductionOrdersService,
    val itemsService: ItemsService,
    val productTreesService: ProductTreesService,
) {

    private val logger = LoggerFactory.getLogger(OrdemProducaoController::class.java)

    /**
     * Ordens de producao da mais nova para a mais antiga. `status` aceita varios valores
     * (PLANEJADA, LIBERADA, FECHADA, CANCELADA ou o nome do SAP); vazio traz todos.
     */
    @GetMapping("")
    fun listar(@RequestParam(required = false) status: List<String>?, page: Pageable): Page<OrdemProducao> {
        return service.get(filtro(status), OrderBy("AbsoluteEntry", Order.DESC), page)
            .tryGetPageValues(page)
    }

    /**
     * Ordem com as linhas. Nem toda versao do Service Layer devolve ItemName na linha, entao o nome
     * que faltar e buscado no cadastro de itens numa consulta so.
     */
    @GetMapping("{id}")
    fun detalhe(@PathVariable id: Int): OrdemProducaoDetalhe {
        val ordem = service.getById(id).tryGetValue<OrdemProducaoDetalhe>()
            .let { it.copy(Produto = it.ItemNo?.let(::produto)) }
        val semNome = ordem.ProductionOrderLines
            .filter { it.ItemName.isNullOrBlank() && it.ItemType != "pit_Text" }
            .mapNotNull { it.ItemNo }
            .distinct()
        if (semNome.isEmpty())
            return ordem
        val nomes = nomesDosItens(semNome)
        return ordem.copy(ProductionOrderLines = ordem.ProductionOrderLines.map {
            if (it.ItemName.isNullOrBlank()) it.copy(ItemName = nomes[it.ItemNo]) else it
        })
    }

    /**
     * Validade (U_ValidadeProduto, em dias), peso e unidade de venda (SWeight1/SalUnitMsr) e a formula
     * do SysFeed (U_LbrOne_Id da estrutura). Complemento do formulario: se o cadastro falhar, a ordem
     * sai sem ele e o operador preenche a mao.
     */
    fun produto(itemCode: String): ProdutoDaOrdem? {
        val item = runCatching { itemsService.getById(itemCode) }
            .onFailure { logger.warn("Cadastro do item {} indisponivel para a ordem de producao", itemCode, it) }
            .getOrNull() ?: return null
        val formula = runCatching { productTreesService.getById(itemCode)["U_LbrOne_Id"] }.getOrNull()
        return ProdutoDaOrdem(
            ValidadeDias = (item["U_ValidadeProduto"] as? Number)?.toInt(),
            PesoSaco = (item["SalesUnitWeight"] as? Number)?.toDouble()?.takeIf { it > 0 },
            UnidadeVenda = item["SalesUnit"]?.toString(),
            Formula = (formula as? Number)?.toInt(),
        )
    }

    private fun nomesDosItens(codigos: List<String>): Map<String, String?> {
        return itemsService.get(Filter(Predicate("ItemCode", codigos, Condicao.IN)), Pageable.ofSize(codigos.size))
            .tryGetValues<Map<String, Any?>>()
            .associate { it["ItemCode"].toString() to it["ItemName"]?.toString() }
    }

    fun filtro(status: List<String>?): Filter {
        val valores = status.orEmpty().filter { it.isNotBlank() }
            .map { StatusOrdemProducao.from(it.trim()).sap }
            .distinct()
        if (valores.isEmpty())
            return Filter()
        return Filter(Predicate("ProductionOrderStatus", valores, Condicao.IN))
    }
}
