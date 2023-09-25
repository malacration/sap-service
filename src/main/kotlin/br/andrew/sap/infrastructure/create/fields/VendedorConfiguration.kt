package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class VendedorConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        userFieldsMDService.findOrCreate(FieldMd("envia_relatorio","Envia Relatorio?","OSLP")
            .also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM"))
                it.defaultValue = "0";
            })
    }
}