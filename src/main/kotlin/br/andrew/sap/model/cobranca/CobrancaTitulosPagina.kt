package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Página de titulos + se a busca de ValorRecebidoNoPeriodo (view auxiliar, teto proprio de
 * paginas do SAP) parou incompleta. Sem isso a tela nao tinha como saber que uma linha da
 * pagina podia estar com esse campo nulo so por causa do teto, nao por falta de recebimento.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class CobrancaTitulosPagina(
    val Titulos: List<CobrancaTitulo>,
    val TruncadoRecebimento: Boolean,
)
