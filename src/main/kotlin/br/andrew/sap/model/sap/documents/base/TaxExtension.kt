package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TaxExtension(
    var VehicleState: String? = null,
    var Incoterms: Int? = null
<<<<<<< HEAD
)
=======
)
>>>>>>> e848f19f65c789d7ec1ccb855c5e1ba198b6c15b
