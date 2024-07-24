package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.model.sap.TableMd
import br.andrew.sap.model.sap.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class FilialVendedorConfiguration(val userFieldsMDService: UserFieldsMDService,
                                  val udoService: UserObjectsMDService,
                                  val  tableService: UserTablesMDService) {

    init {
        listOf(
            TableMd(
                "RO_FILIAL_VENDEDOR","Filial Vendedor", TbType.bott_Document
            ),
            TableMd(
                "RO_FILIAL_LINHA","Linhas Filial Vendedor ", TbType.bott_DocumentLines
            )
        ).forEach{ tableService.findOrCreate(it)}

        listOf(
            FieldMd("filial","Filial","@RO_FILIAL_LINHA")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("2","Matriz")
                ) },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }


}