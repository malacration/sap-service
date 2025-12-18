package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["producao.field"], havingValue = "true", matchIfMissing = true)
class addBusinessPartnersFieldsConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(
            // TODO ajustar subtype depois para price
            FieldMd("productionCost","Custo Apontado p/ Produção","OITM",DbType.db_Numeric),
        )
            .forEach { userFieldsMDService.findOrCreate(it) }
    }
}