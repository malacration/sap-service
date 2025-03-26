package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.UDOObjType
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.structs.UserObjectsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["ord.carregamento"], havingValue = "true", matchIfMissing = false)
class OrdemCarregamentoConfiguration(val udoService: UserObjectsMDService) {

    init {
        val udoProperties = getUserDefined()
        udoService.findOrCreate(udoProperties)
    }

    private fun getUserDefined(): UserDefinedObject {
        return UserDefinedObject(
            "RO_ORD_CARREGAMENTO",
            "Ordem de Carregamento",
            "RO_ORD_CARREGAMENTO",
            UDOObjType.boud_Document,
            YesNo.tYES
        )
    }
}
