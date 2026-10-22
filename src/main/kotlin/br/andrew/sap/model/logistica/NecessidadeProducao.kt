package br.andrew.sap.model.logistica

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/** Deposito que a tela oferece no filtro, ja agrupavel pela filial. */
data class DepositoNecessidade(
    val whsCode: String,
    val whsName: String,
    val filial: Int,
    val filialNome: String?,
    val estoque: BigDecimal,
)

/**
 * Uma linha da necessidade de producao. Todas as quantidades na unidade de estoque do item.
 *
 * aFabricar = estoque - pedido: negativo e o que falta produzir para atender o que ja foi pedido.
 * semPrevisao = vendido sem data de entrega: saldo de contrato de venda futura que ainda nao virou
 * pedido + o que falta entregar das notas de simples faturamento.
 */
data class NecessidadeProducaoLinha(
    val itemCode: String,
    val itemName: String,
    val grupo: String?,
    /** U_linha_sustennutri: codigo da linha de produto (premium, mega...); a tela agrupa por ela. */
    val linha: String?,
    /** Descricao do valor valido (OX PREMIUM); nulo se o codigo nao estiver cadastrado. */
    val linhaNome: String?,
    /** Posicao do valor valido no SAP, para ordenar os grupos. */
    val linhaOrdem: Int?,
    val unidade: String?,
    val estoque: BigDecimal,
    val pedido: BigDecimal,
    val contratoVendaFutura: BigDecimal,
    val simplesFaturamento: BigDecimal,
) {
    // Nome fixo: pelo getter (getAFabricar) o Jackson serializaria "afabricar".
    @get:JsonProperty("aFabricar")
    val aFabricar: BigDecimal get() = estoque - pedido

    @get:JsonProperty("semPrevisao")
    val semPrevisao: BigDecimal get() = contratoVendaFutura + simplesFaturamento
}

data class NecessidadeProducao(
    val linhas: List<NecessidadeProducaoLinha>,
    /** Depositos em que o estoque foi somado - os marcados menos os excluidos por configuracao. */
    val depositos: List<String>,
    /** Filiais em que a venda (pedido e sem previsao) foi somada. */
    val filiais: List<Int>,
    /** Configuracao vigente, para a tela explicar o recorte. */
    val depositosExcluidos: List<String>,
    val utilizacoesSimplesFaturamento: List<Int>,
)

/** O que se abre ao clicar numa coluna: a coluna Pedido ou a Sem previsao. */
enum class DetalheNecessidade { PEDIDO, SEM_PREVISAO }

/** Origem de um documento do detalhe. */
enum class TipoDocumentoNecessidade { PEDIDO, VENDA_FUTURA, SIMPLES_FATURAMENTO }

/**
 * Um documento por tras de uma celula da necessidade de producao, na unidade de estoque.
 *
 * quantidade e o total do documento para o item; entregue ja desconta a devolucao (venda futura);
 * emPedido so existe no contrato (retirada ja pedida); saldo e o que entra na celula.
 */
data class DocumentoNecessidade(
    val tipo: TipoDocumentoNecessidade,
    val docEntry: Int,
    val docNum: Int,
    val data: String?,
    val previsao: String?,
    val cardCode: String?,
    val cardName: String?,
    val filial: Int,
    val filialNome: String?,
    val deposito: String?,
    val vendedor: String?,
    val contratoVendaFutura: Int?,
    val quantidade: BigDecimal,
    val entregue: BigDecimal,
    val emPedido: BigDecimal,
    val saldo: BigDecimal,
)

data class NecessidadeProducaoDetalhe(
    val itemCode: String,
    val detalhe: DetalheNecessidade,
    val documentos: List<DocumentoNecessidade>,
    /** Filiais em que a venda foi somada. */
    val filiais: List<Int>,
) {
    val total: BigDecimal get() = documentos.fold(BigDecimal.ZERO) { soma, it -> soma + it.saldo }
}
