package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class BusinessPartnersFieldsConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(FieldMd("keyUpdate","Atualização pedido","OCRD",DbType.db_Memo))
            .forEach { userFieldsMDService.findOrCreate(it) }
    }
}