package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.*
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.Elements
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UserKeyMD
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserKeyMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ReferenciaComercialConfiguration(val userFieldsMDService: UserFieldsMDService,
                                       val  tableService: UserTablesMDService,
                                       val userKeyMD: UserKeyMDService) {

    init {

        tableService.findOrCreate(
            TableMd(
            "REFRENCIACOMERCIAL","Referencias Comercial", TbType.bott_MasterData
        ))

        tableService.findOrCreate(TableMd(
            "referencia","Referencia",TbType.bott_MasterDataLines
        ))

        listOf(
            FieldMd("nome","Nome da Referencia","@Referencia", DbType.db_Alpha),
            FieldMd("telefone","Numero Telefone","@Referencia", DbType.db_Alpha),
            FieldMd("anotacao","Anotação","@Referencia", DbType.db_Memo),
            FieldMd("cardCode","Parceiro de Negócio","@REFRENCIACOMERCIAL", DbType.db_Alpha,YesNo.tYES),
        ).forEach { userFieldsMDService.findOrCreate(it) }


//        listOf(
//            FieldMd("referencias","Referencias","REFRENCIACOMERCIAL",DbType.db_Alpha)
//                .also { it.linkedUDO = "referencia" },
//        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            UserKeyMD("uniquiPn","@REFRENCIACOMERCIAL",YesNo.tYES,listOf(Elements("cardCode"))),
        ).forEach { userKeyMD.findOrCreate(it) }

    }
}