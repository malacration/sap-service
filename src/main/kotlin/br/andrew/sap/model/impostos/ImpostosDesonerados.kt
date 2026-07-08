package br.andrew.sap.model.impostos

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// Codigos (JurisdictionType/STAType) dos impostos desonerados, vindos da config.
// Bean injetavel diretamente onde precisar (ex.: controller), para usar no modelo
// sem depender do DesoneradoService.
@Component
class ImpostosDesonerados(
    @Value("\${imposto.icms.desonerado:0}") val tipoImposto: List<Int>,
    @Value("\${imposto.icms.desonerado.futuro:0}") val tipoImpostoFuturo: List<Int>,
) {
    val ids: List<Int> = tipoImposto + tipoImpostoFuturo
}
