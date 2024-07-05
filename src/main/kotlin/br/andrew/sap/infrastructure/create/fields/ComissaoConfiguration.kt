package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.TableMd
import br.andrew.sap.model.TbType
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UdoService
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ComissaoConfiguration(val userFieldsMDService: UserFieldsMDService,
                            val udoService: UdoService,
                            val  tableService: UserTablesMDService
) {
    init {
        tableService.findOrCreate(
            TableMd(
            "COMISSAO","Tabela com regras de comissao", TbType.bott_MasterData
        ))
        tableService.findOrCreate(TableMd(
            "condicoesFV","Linha condiçoes ",TbType.bott_MasterDataLines
        ))
        tableService.findOrCreate(TableMd(
            "LiberaPara","Libera para",TbType.bott_MasterDataLines
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

        listOf(
            ""
        ).forEach(
            udoService.findOrCreate(it)
        )

        listOf(
            FieldMd("tipoComissao","Selecionar Comissao","OPLN", DbType.db_Alpha)
                .also { it.linkedUDO = "comissao" },
        ).forEach { userFieldsMDService.findOrCreate(it) }



    }
}