package br.andrew.sap.model.logistica

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Uma linha do relatorio de ticket medio de frete: uma localidade, quantas
 * notas de entrega ela teve no periodo e quanto de frete foi cobrado no total.
 *
 * TicketMedio nao vem do SQL - a divisao e feita aqui pra controlar o
 * arredondamento (2 casas) e nao depender de divisao no validador de
 * SQLQueries do SAP.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class TicketFreteLocalidade(
    val CodLocalidade: String?,
    val Localidade: String?,
    val Notas: Int,
    val TotalFrete: BigDecimal
) {
    /**
     * Quantidade de produtos vendidos nessas mesmas notas. Nao vem da query de
     * frete (juntar as linhas do documento com as linhas de despesa no mesmo
     * SELECT multiplicaria as linhas e inflaria as duas somas) - e preenchida
     * por TicketFreteService a partir de uma segunda view.
     */
    var Quantidade: BigDecimal = BigDecimal.ZERO

    val TicketMedio: BigDecimal
        get() = if (Notas <= 0)
            BigDecimal.ZERO
        else
            TotalFrete.divide(BigDecimal(Notas), 2, RoundingMode.HALF_UP)

    /** Frete medio por produto vendido - o mesmo frete, dividido pelo volume em vez de por nota. */
    val TicketMedioProduto: BigDecimal
        get() = if (Quantidade <= BigDecimal.ZERO)
            BigDecimal.ZERO
        else
            TotalFrete.divide(Quantidade, 2, RoundingMode.HALF_UP)
}

/** Linha da view de quantidade: so o par localidade/quantidade, unido ao frete no service. */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class QuantidadeLocalidade(
    val CodLocalidade: String?,
    val Quantidade: BigDecimal
)
