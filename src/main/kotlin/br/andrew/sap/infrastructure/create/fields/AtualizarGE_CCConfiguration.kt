package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class AtualizarGE_CCConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(
            FieldMd("Atualizar_Observacao_Grupo_CP", "Atualização Observação Grupo CP", "OJDT", DbType.db_Numeric).also {
                it.defaultValue = "0"
            }
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }
}