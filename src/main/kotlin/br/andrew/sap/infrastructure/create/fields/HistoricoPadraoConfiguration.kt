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
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class BusinessPartnersFieldsConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(
            FieldMd("keyUpdate","Atualização pedido","OCRD",DbType.db_Alpha),
            FieldMd("dataSerasa","Data Serasa","OCRD",DbType.db_Date)
        )
            .forEach { userFieldsMDService.findOrCreate(it) }
    }
}