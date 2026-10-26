package br.andrew.sap.model.sap.address

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
//IbgeCode: codigo IBGE de 7 digitos (OCNT) - e por ele que a busca de CEP (ViaCEP) acha o municipio,
//ja que o AbsId e interno do SAP e o nome nao tem grafia padrao no cadastro
class City(val AbsId : String, val Country : String, val Name : String, val IbgeCode : String? = null) {

}
