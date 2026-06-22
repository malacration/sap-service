package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.sysfeed"], havingValue = "true", matchIfMissing = false)
class SysfeedFieldsConfiguration(
    private val userFieldsMDService: UserFieldsMDService
) {
    init {
        listOf("OPDN", "OPCH", "OCRD", "OWOR").forEach { table ->
            createStatusField(table)
        }
    }

    private fun createStatusField(table: String) {
        FieldMd("sysfeed_status", "Status Sysfeed", table).also {
            it.ValidValuesMD = listOf(
                ValuesMd("PENDENTE", "Pendente"),
                ValuesMd("ENVIADO", "Enviado"),
                ValuesMd("DUPLICADO", "Duplicado"),
                ValuesMd("ERRO", "Erro"),
                ValuesMd("PARCIAL", "Parcial")
            )
            it.defaultValue = "PENDENTE"
            userFieldsMDService.findOrCreate(it)
        }
    }
}
