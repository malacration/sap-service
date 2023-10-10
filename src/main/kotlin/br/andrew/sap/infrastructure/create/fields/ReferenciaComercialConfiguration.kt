package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.*
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class ReferenciaComercialConfiguration(val userFieldsMDService: UserFieldsMDService,
                                       val  tableService: UserTablesMDService) {

    init {

        tableService.findOrCreate(
            TableMd(
            "REFRENCIACOMERCIAL","Referencias Comercial", TbType.bott_MasterData
        ))

        tableService.findOrCreate(TableMd(
            "referencia","Libera para",TbType.bott_MasterDataLines
        ))

//        listOf(
//            FieldMd("referencias","Referencias","REFRENCIACOMERCIAL",DbType.db_Alpha)
//                .also { it.linkedUDO = "referencia" },
//        ).forEach { userFieldsMDService.findOrCreate(it) }


        listOf(
            FieldMd("nome","Nome da Referencia","@Referencia", DbType.db_Alpha),
            FieldMd("telefone","Numero Telefone","@Referencia", DbType.db_Alpha),
            FieldMd("anotacao","Anotação","@Referencia", DbType.db_Memo),
        ).forEach { userFieldsMDService.findOrCreate(it) }



    }
}