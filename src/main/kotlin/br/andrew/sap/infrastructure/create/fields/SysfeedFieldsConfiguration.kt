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
        createNumeroSysfeedField("OWOR")
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

    // Numero atribuido pelo sigafran ao enviar a ordem de producao ao SYSFEED, gravado de
    // volta no SAP so para rastreabilidade (nao e usado como chave anti-duplicidade).
    // Texto (nao numerico) porque e so exibicao/consulta; 10 chars cobre o CodOrdemProducao (max 10 digitos).
    private fun createNumeroSysfeedField(table: String) {
        FieldMd("sysfeed_numero", "Numero Sysfeed", table).also {
            it.size = 10
            userFieldsMDService.findOrCreate(it)
        }
    }
}
