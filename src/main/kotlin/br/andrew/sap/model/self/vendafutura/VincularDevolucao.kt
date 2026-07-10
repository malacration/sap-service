package br.andrew.sap.model.self.vendafutura

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Corpo do request para vincular uma devolução avulsa (nota de crédito lançada solta no
 * SAP B1) a um contrato de venda futura.
 *
 * - [devolucaoDocNum]: número (DocNum) da devolução, digitado pelo usuário — a devolução é
 *   avulsa e por isso não aparece nas entregas já carregadas do contrato.
 * - [notaSaidaDocEntry]: DocEntry da nota de saída, selecionada pelo usuário na lista de
 *   entregas que já está carregada na tela do contrato.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class VincularDevolucao(
    val devolucaoDocNum: Int,
    val notaSaidaDocEntry: Int,
)
