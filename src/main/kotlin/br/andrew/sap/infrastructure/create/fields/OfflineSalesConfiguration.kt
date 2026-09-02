package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineSalesConfiguration(userFieldsMDService: UserFieldsMDService) {
    init {
        userFieldsMDService.findOrCreate(
            FieldMd("offline_id", "ID transmissao offline", "OQUT", DbType.db_Alpha).also { it.size = 64 }
        )
        userFieldsMDService.findOrCreate(
            FieldMd("offline_user", "Usuario transmissao offline", "OQUT", DbType.db_Alpha).also { it.size = 64 }
        )
    }
}
