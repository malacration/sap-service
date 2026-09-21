package br.andrew.sap.model.comercial.painel

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Filtros do painel. Todos viram bind parameters - nada e concatenado no SQL.
 *
 * `filiais` e `slpCode` sao PEDIDOS do usuario, nunca a autorizacao: o escopo
 * efetivo e recalculado no backend a partir do token (ver PainelVendasV2Service).
 */
data class PainelFiltro(
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    val filiais: List<Int> = listOf(),
    val slpCode: Int? = null,
    val granularidade: Granularidade = Granularidade.MES,
)

/**
 * Granularidade da serie temporal.
 *
 * E um enum, e nao texto livre, porque vira FRAGMENTO de SQL - e fragmento nao
 * pode ser bind parameter. A whitelist fechada e o que impede injecao aqui.
 */
enum class Granularidade { DIA, SEMANA, MES }

data class PainelKpis(
    /**
     * Faturamento COM impostos, como consta no documento.
     *
     * Nao existe aqui um "sem impostos": `DocTotal - VatSum` nao representa
     * receita liquida no Brasil (PIS/COFINS e ICMS sao por dentro; IPI e ST por
     * fora), e nesta base o ICMS desonerado ainda majora o LineTotal. Publicar
     * esse numero seria inventar precisao que o dado nao tem.
     */
    val faturamentoBruto: BigDecimal,
    val devolucoes: BigDecimal,
    val faturamentoLiquido: BigDecimal,
    val qtdFaturas: Int,
    val qtdDevolucoes: Int,
    val clientesAtivos: Int,
    val filiaisComVenda: Int,
    /**
     * Faturamento SOMENTE das linhas de produto (exclui documentos de servico).
     * Diverge de [faturamentoBruto] de proposito - a tela precisa deixar claro
     * que preco medio nao cobre o mesmo universo que o faturamento.
     */
    val valorProdutos: BigDecimal,
    /** Quantidade em UM de ESTOQUE (`InvQty`). Ver a ressalva em [precoMedio]. */
    val qtdItens: BigDecimal,
    /**
     * Nulo quando nao ha faturas no periodo - inclusive quando venda e
     * cancelamento se anulam dentro do mesmo recorte. Melhor vazio na tela do
     * que um numero inventado.
     */
    val ticketMedio: BigDecimal?,
    /**
     * Faturamento dividido pelo numero de clientes distintos que compraram.
     * Nulo quando nao houve cliente ativo no periodo.
     */
    val ticketMedioPorCliente: BigDecimal?,
    /**
     * Valor das linhas de produto dividido pela quantidade vendida.
     *
     * ATENCAO: soma quantidades de produtos com UNIDADES DE MEDIDA DIFERENTES
     * (kg, unidade, caixa). O numero so e comparavel dentro de um mesmo produto
     * ou grupo homogeneo; no total do painel ele serve como tendencia, nao como
     * preco real de nada. Por isso [precoMedioConfiavel] acompanha o valor.
     */
    val precoMedio: BigDecimal?,
    /**
     * false quando o recorte mistura unidades de medida - hoje sempre false no
     * nivel do painel. A tela usa isso para exibir o aviso junto do numero.
     */
    val precoMedioConfiavel: Boolean,
    /** Recorte efetivamente aplicado, para a tela poder exibir o que foi consultado. */
    val escopo: EscopoAplicado,
)

data class EscopoAplicado(
    /** Filiais consultadas. Irrelevante quando [todasFiliais] e true. */
    val filiais: List<Int>,
    /** true quando nenhuma restricao de filial foi aplicada (admin sem filtro). */
    val todasFiliais: Boolean,
    val todosVendedores: Boolean,
    val vendedor: Int?,
)

/**
 * Um ponto da serie temporal, com o valor do periodo atual e o do mesmo periodo
 * do ano anterior, ja alinhados no mesmo eixo.
 *
 * `anoAnterior` e nulo quando nao houve movimento naquele periodo um ano atras -
 * diferente de zero, que significa "houve movimento e deu zero".
 */
data class PontoEvolucao(
    val periodo: String,
    val faturamento: BigDecimal,
    val qtdFaturas: Int,
    val anoAnterior: BigDecimal?,
)
