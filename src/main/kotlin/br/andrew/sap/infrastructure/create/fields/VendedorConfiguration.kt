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
class VendedorConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        listOf(
            FieldMd("envia_relatorio","Envia Relatorio?","OSLP").also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                it.defaultValue = "0";
            },
            FieldMd("password","senha","OSLP",DbType.db_Memo)
        ).forEach{userFieldsMDService.findOrCreate(it)}
    }
}