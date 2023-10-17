package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class PessoaContatoConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {

        userFieldsMDService.findOrCreate(
            FieldMd("tipoPessoa","Tipo Pessoa","OCPR")
            .also {
                it.ValidValuesMD = listOf(
                    ValuesMd("-1","SELECIONE"),
                    ValuesMd("conjuge","Cônjuge"),
                    ValuesMd("colaborador","Colaborador")
                )
                it.defaultValue = "-1";
            })
    }
}