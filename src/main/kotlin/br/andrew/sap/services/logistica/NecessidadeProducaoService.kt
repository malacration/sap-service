package br.andrew.sap.services.logistica

import br.andrew.sap.model.logistica.DepositoNecessidade
import br.andrew.sap.model.logistica.DetalheNecessidade
import br.andrew.sap.model.logistica.DocumentoNecessidade
import br.andrew.sap.model.logistica.NecessidadeProducao
import br.andrew.sap.model.logistica.NecessidadeProducaoDetalhe
import br.andrew.sap.model.logistica.NecessidadeProducaoLinha
import br.andrew.sap.model.logistica.TipoDocumentoNecessidade
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Necessidade de producao: estoque x pedido x vendido sem previsao, por produto.
 *
 * Dois recortes independentes, sempre recalculados aqui a partir do que o usuario marcou:
 *  - depositos: de onde sai o Estoque. So os da faixa de produtos, fora os excluidos por configuracao.
 *  - filiais: de onde sai a venda - Pedido e Sem previsao (contrato e nota de simples faturamento),
 *    cada documento pela propria filial.
 * Marcar uma filial nao limita os depositos: da para ver a venda de uma filial contra o estoque da
 * fabrica mais o da filial.
 */
@Service
class NecessidadeProducaoService(
    private val odbc: OdbcClient,
    /**
     * Depositos que nunca entram na conta (refugo, descarte, nao conformidade). Lista separada por
     * virgula; variavel de ambiente NECESSIDADE_PRODUCAO_DEPOSITOS_EXCLUIDOS.
     */
    @Value("\${necessidade-producao.depositos-excluidos:\${NECESSIDADE_PRODUCAO_DEPOSITOS_EXCLUIDOS:}}")
    depositosExcluidos: List<String> = listOf(),
    /**
     * Utilizacoes de simples faturamento (OUSG.ID). O pedido delas sai da coluna Pedido e o que falta
     * entregar das notas delas entra em Sem previsao. Variavel NECESSIDADE_PRODUCAO_UTILIZACOES_SIMPLES_FATURAMENTO.
     */
    @Value("\${necessidade-producao.utilizacoes-simples-faturamento:\${NECESSIDADE_PRODUCAO_UTILIZACOES_SIMPLES_FATURAMENTO:16}}")
    utilizacoesSimplesFaturamento: List<Int> = listOf(16),
) {
    private val depositosExcluidos = depositosExcluidos.map { it.trim() }.filter { it.isNotEmpty() }
    private val utilizacoes = utilizacoesSimplesFaturamento

    private val sqlDepositos by lazy { SqlResource.carregar("odbc/necessidade-producao-depositos.sql") }
    private val sqlNecessidade by lazy { SqlResource.carregar("odbc/necessidade-producao.sql") }
    private val sqlPedidos by lazy { SqlResource.carregar("odbc/necessidade-producao-pedidos.sql") }
    private val sqlSemPrevisao by lazy { SqlResource.carregar("odbc/necessidade-producao-sem-previsao.sql") }

    fun depositos(itemDe: String, itemAte: String): List<DepositoNecessidade> {
        val faixa = faixa(itemDe, itemAte)
        return odbc.consultar(sqlDepositos, mapOf("itemDe" to faixa.first, "itemAte" to faixa.second), MAX_DEPOSITOS)
            .rows
            .map {
                DepositoNecessidade(
                    whsCode = it["WhsCode"].toString(),
                    whsName = it["WhsName"]?.toString() ?: "",
                    filial = decimal(it["BPLId"]).toInt(),
                    filialNome = it["BPLName"]?.toString(),
                    estoque = decimal(it["Estoque"]),
                )
            }
            .filter { it.whsCode !in depositosExcluidos }
    }

    /**
     * @param depositos vazio = todos os depositos da faixa (fora os excluidos)
     * @param filiais vazio = todas as filiais dos depositos da faixa
     */
    fun consultar(itemDe: String, itemAte: String, filiais: List<Int>, depositos: List<String>): NecessidadeProducao {
        val faixa = faixa(itemDe, itemAte)
        val recorte = recorte(faixa, filiais, depositos)
        val params = mapOf(
            "itemDe" to faixa.first,
            "itemAte" to faixa.second,
            "depositos" to recorte.depositos,
            "filiais" to recorte.filiais,
            "utilizacoes" to utilizacoesParaIn(),
        )
        val linhas = odbc.consultar(sqlNecessidade, params, MAX_PRODUTOS).rows.map {
            NecessidadeProducaoLinha(
                itemCode = it["ItemCode"].toString(),
                itemName = it["ItemName"]?.toString() ?: "",
                grupo = it["Grupo"]?.toString(),
                linha = it["Linha"]?.toString()?.takeIf { valor -> valor.isNotBlank() },
                linhaNome = it["LinhaNome"]?.toString(),
                linhaOrdem = it["LinhaOrdem"]?.let { valor -> decimal(valor).toInt() },
                unidade = it["Unidade"]?.toString(),
                estoque = decimal(it["Estoque"]),
                pedido = decimal(it["Pedido"]),
                contratoVendaFutura = decimal(it["ContratoVendaFutura"]),
                simplesFaturamento = decimal(it["SimplesFaturamento"]),
            )
        }
        return NecessidadeProducao(
            linhas = linhas.sortedWith(compareBy({ it.aFabricar }, { it.itemCode })),
            depositos = recorte.depositos,
            filiais = recorte.filiais,
            depositosExcluidos = depositosExcluidos,
            utilizacoesSimplesFaturamento = utilizacoes,
        )
    }

    /**
     * Documentos por tras de uma celula: os pedidos da coluna Pedido ou os contratos e notas da
     * Sem previsao. O recorte e o mesmo do relatorio, refeito para o produto, entao a soma dos
     * saldos fecha com a celula clicada.
     */
    fun detalhe(itemCode: String, detalhe: DetalheNecessidade, filiais: List<Int>): NecessidadeProducaoDetalhe {
        val item = faixa(itemCode, itemCode).first
        val filiaisEfetivas = filiais(depositos(item, item), filiais)
        val params = mapOf(
            "itemCode" to item,
            "filiais" to filiaisEfetivas,
            "utilizacoes" to utilizacoesParaIn(),
        )
        val sql = when (detalhe) {
            DetalheNecessidade.PEDIDO -> sqlPedidos
            DetalheNecessidade.SEM_PREVISAO -> sqlSemPrevisao
        }
        val documentos = odbc.consultar(sql, params, MAX_DOCUMENTOS).rows.map { documento(it) }
        return NecessidadeProducaoDetalhe(item, detalhe, documentos, filiaisEfetivas)
    }

    private class Recorte(val depositos: List<String>, val filiais: List<Int>)

    private fun recorte(faixa: Pair<String, String>, filiais: List<Int>, depositos: List<String>): Recorte {
        val possiveis = depositos(faixa.first, faixa.second)
        val efetivos = possiveis.filter { depositos.isEmpty() || it.whsCode in depositos }.map { it.whsCode }
        if (efetivos.isEmpty())
            throw NecessidadeProducaoException("Nenhum deposito selecionado tem os produtos da faixa informada.")
        return Recorte(efetivos, filiais(possiveis, filiais))
    }

    /** Filiais da venda: as pedidas que existem nos depositos da faixa; nenhuma pedida = todas. */
    private fun filiais(possiveis: List<DepositoNecessidade>, filiais: List<Int>): List<Int> {
        // Filial 0 e deposito sem filial: nenhum documento de venda e lancado nela.
        val conhecidas = possiveis.map { it.filial }.filter { it != 0 }.distinct().sorted()
        val efetivas = if (filiais.isEmpty()) conhecidas else conhecidas.filter { it in filiais }
        if (efetivas.isEmpty())
            throw NecessidadeProducaoException("Nenhuma filial selecionada vende os produtos da faixa informada.")
        return efetivas
    }

    // IN () vazio e erro de sintaxe; -1 nao e utilizacao nenhuma.
    private fun utilizacoesParaIn() = utilizacoes.ifEmpty { listOf(-1) }

    private fun documento(it: Map<String, Any?>) = DocumentoNecessidade(
        tipo = TipoDocumentoNecessidade.valueOf(it["Tipo"].toString()),
        docEntry = decimal(it["DocEntry"]).toInt(),
        docNum = decimal(it["DocNum"]).toInt(),
        data = it["Data"]?.toString(),
        previsao = it["Previsao"]?.toString(),
        cardCode = it["CardCode"]?.toString(),
        cardName = it["CardName"]?.toString(),
        filial = decimal(it["BPLId"]).toInt(),
        filialNome = it["BPLName"]?.toString(),
        deposito = it["Deposito"]?.toString(),
        vendedor = it["Vendedor"]?.toString(),
        contratoVendaFutura = it["ContratoVendaFutura"]?.let { valor -> decimal(valor).toInt() },
        quantidade = decimal(it["Quantidade"]),
        entregue = decimal(it["Entregue"]),
        emPedido = decimal(it["EmPedido"]),
        saldo = decimal(it["Saldo"]),
    )

    private fun faixa(itemDe: String, itemAte: String): Pair<String, String> {
        val de = itemDe.trim().uppercase()
        val ate = itemAte.trim().uppercase()
        if (de.isEmpty() || ate.isEmpty())
            throw NecessidadeProducaoException("Informe o produto inicial e o final da faixa.")
        if (de.length > 50 || ate.length > 50)
            throw NecessidadeProducaoException("Codigo de produto invalido.")
        return if (de <= ate) de to ate else ate to de
    }

    /** O sap-odbc manda BigDecimal como texto; ler como Double perderia casas. */
    private fun decimal(valor: Any?): BigDecimal = when (valor) {
        null -> BigDecimal.ZERO
        is BigDecimal -> valor
        else -> valor.toString().trim().toBigDecimalOrNull()
            ?: throw IllegalStateException("Valor nao numerico vindo da consulta: '$valor'")
    }

    private companion object {
        const val MAX_DEPOSITOS = 1000
        const val MAX_PRODUTOS = 5000
        const val MAX_DOCUMENTOS = 5000
    }
}

class NecessidadeProducaoException(override val message: String) : RuntimeException(message)
