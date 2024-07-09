package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.TableMd
import br.andrew.sap.model.TbType
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ComissaoConfiguration(val userFieldsMDService: UserFieldsMDService,
                            @Value("\${comissao.delete:false}") isDelete : Boolean,
                            val udoService: UserObjectsMDService,
                            val  tableService: UserTablesMDService
) {
    init {
        //TODO fazer parametro
        tableService.findOrCreate(
            TableMd(
            "COMISSAO","Tabela com regras de comissao", TbType.bott_Document
        ))
        tableService.findOrCreate(TableMd(
            "condicoesFV","Linha condiçoes ",TbType.bott_DocumentLines
        ))
        tableService.findOrCreate(TableMd(
            "LiberaPara","Libera para",TbType.bott_DocumentLines
        ))

        listOf(
            FieldMd("porcentagem","comissão em porcentagem","@COMISSAO", DbType.db_Float),
            FieldMd("desconto","Desconto (%) do vendedor","@COMISSAO", DbType.db_Float),
            FieldMd("desconto","Desconto (%)","@condicoesFV", DbType.db_Float),
            FieldMd("juros","Juros (%)","@condicoesFV", DbType.db_Float),
            FieldMd("Filial","Filial","@LiberaPara")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("0","Nenhuma")
                ) },
        ).forEach { userFieldsMDService.findOrCreate(it) }


        //TODO criar objeto do usuario

//        listOf(
//            ""
//        ).forEach(
//            udoService.findOrCreate(it)
//        )

        listOf(
            FieldMd("tipoComissao","Selecionar Comissao","OPLN", DbType.db_Alpha)
                .also { it.linkedUDO = "comissao" },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }


    fun comissaoObject(): UserDefinedObject {
        return UserDefinedObject("comissao", "Comissões", "COMISSAO",)
    }
}