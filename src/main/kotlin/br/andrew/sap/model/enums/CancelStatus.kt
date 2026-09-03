package br.andrew.sap.model.enums

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * CancelStatus do SAP (OINV/ODPI/ORIN."CANCELED"), o unico campo que separa os dois
 * lados de um cancelamento - o campo Cancelled (tYES/tNO) nao faz essa distincao:
 * - csNo: documento normal, valendo;
 * - csYes / csCancelled: o documento original, estornado;
 * - csCancellationDocument: o documento que o SAP cria pra estornar o original.
 * Tudo que nao e csNo esta fora do jogo - ver cancelado() e MapaRelacoesService.
 */
enum class CancelStatus {
    csNo, csYes, csCancelled, csCancellationDocument;

    fun cancelado(): Boolean = this != csNo

    companion object {
        val column : String = "CancelStatus"

        /**
         * Desserializacao tolerante: valor que nao conhecemos vira null em vez de estourar
         * InvalidFormatException. Esse campo esta no Document, ou seja, na base de TODO
         * documento - um valor novo do SAP aqui derrubava a leitura da nota inteira e,
         * com ela, o fluxo que estivesse rodando (foi o que aconteceu com "csYes" na
         * ConciliacaoVendaFuturaSchedule). CancelStatus e informativo: sem ele o mapa
         * deixa de marcar o cancelamento, o que e MUITO melhor do que quebrar o processo.
         */
        @JvmStatic
        @JsonCreator
        fun from(valor: String?): CancelStatus? {
            return values().firstOrNull { it.name.equals(valor?.trim(), ignoreCase = true) }
        }
    }
}
