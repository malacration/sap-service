package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.*
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class TableConfiguration(val userFieldsMDService: UserFieldsMDService,
                        val  tableService: UserTablesMDService) {

    init {
        tableService.findOrCreate(TableMd(
           "COMISSAO","Tabela com regras de comissao",TbType.bott_MasterData
       ))

        listOf(
            FieldMd("porcentagem","comissão em porcentagem","@COMISSAO", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("desconto","Desconto (%) do vendedor","@COMISSAO", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }


        listOf(
            FieldMd("tipoComissao","Selecionar Comissao","OPLN", DbType.db_Alpha)
                .also { it.linkedUDO = "comissao" },
        ).forEach { userFieldsMDService.findOrCreate(it) }

       tableService.findOrCreate(TableMd(
            "condicoesFV","Linha condiçoes ",TbType.bott_MasterDataLines
        ))
        tableService.findOrCreate(TableMd(
            "LiberaPara","Libera para",TbType.bott_MasterDataLines
        ))
        listOf(
            FieldMd("desconto","Desconto (%)","@condicoesFV", DbType.db_Float),
            FieldMd("juros","Juros (%)","@condicoesFV", DbType.db_Float),
            FieldMd("Filial","Filial","@LiberaPara")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("2","SUSTENNUTRI NUTRICAO ANIMAL"),
                    ValuesMd("4","SUSTENNUTRI NUTRICAO ANIMAL LTDA - FILIAL- AC"),
                    ValuesMd("11","SUSTENNUTRI NUTRICAO ANIMAL LTDA - Filial - RO"),
                    ValuesMd("0","Nenhuma")
                ) },
        ).forEach { userFieldsMDService.findOrCreate(it) }

    }
}