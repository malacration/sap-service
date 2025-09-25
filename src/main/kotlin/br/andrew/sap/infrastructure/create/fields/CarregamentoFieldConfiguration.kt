package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.field2"], havingValue = "true", matchIfMissing = false)
class CarregamentoFieldConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(
            FieldMd("ORD_CARREGAMENTO2", "Ordem Carregamento 2", "RDR1", DbType.db_Alpha)
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }
    }
}
