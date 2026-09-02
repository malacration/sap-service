package br.andrew.sap.model.impostos

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Predicate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// Codigos (JurisdictionType/STAType) dos impostos desonerados, vindos da config.
// Bean injetavel diretamente onde precisar (ex.: controller), para usar no modelo
// sem depender do DesoneradoService.
@Component
class ImpostosDesonerados(
    @Value("\${imposto.icms.desonerado:0}") val tipoImposto: List<Int>,
    @Value("\${imposto.icms.desonerado.futuro:0}") val tipoImpostoFuturo: List<Int>,
    // Filiais (BPLId) que calculam desonerado. Sem isso os schedules puxavam documento de
    // filial que nao tem a regra e mexiam no preco a toa. Mesma lista da trava
    // SBO_SP_VALIDACAO_VENDA e do job do RD Station.
    //
    // SEM DEFAULT de proposito: qual filial calcula desonerado e decisao fiscal, nao pode ser
    // herdada por engano de um valor no codigo. Ambiente sem o parametro nao sobe.
    @Value("\${imposto.icms.desonerado.filiais}") val filiais: List<Int>,
) {
    init {
        // Falha na criacao do bean, entao o contexto nao sobe. E de proposito: lista vazia
        // renderiza filtro OData quebrado (o Condicao.IN devolve string vazia e o Filter junta
        // tudo com " and "), e schedule sem recorte de filial mexe em documento que nao devia.
        // Melhor a aplicacao nao subir do que subir calculando desonerado onde nao deve.
        if(filiais.isEmpty())
            throw IllegalStateException(
                "O parametro [imposto.icms.desonerado.filiais] e obrigatorio e nao pode ser vazio. " +
                "Informe os BPLId das filiais que calculam desonerado, ex.: 2,4,11,17,18")
    }

    val ids: List<Int> = tipoImposto + tipoImpostoFuturo

    /** Predicado de filial para o filtro OData dos schedules. Nunca nulo: ver o [init]. */
    fun filtroFiliais(): Predicate {
        return Predicate("BPL_IDAssignedToInvoice", filiais, Condicao.IN)
    }
}
