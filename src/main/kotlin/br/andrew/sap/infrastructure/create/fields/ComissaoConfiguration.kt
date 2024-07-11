package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.TableMd
import br.andrew.sap.model.TbType
import br.andrew.sap.model.entity.*
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ComissaoConfiguration(val userFieldsMDService: UserFieldsMDService,
                            @Value("\${comissao.delete:false}") isDelete : Boolean,
                            val udoService: UserObjectsMDService,
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

            FieldMd("regressiva","Comissão regressiva?","COMISSAO")
                .also {
                    it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                    it.defaultValue = "0"
                },

            FieldMd("desconto","Desconto (%)","@condicoesFV", DbType.db_Float),
            FieldMd("juros","Juros (%)","@condicoesFV", DbType.db_Float),

//            FieldMd("prazo","Prazo","@condicoesFV", DbType.db_Numeric).also {
//                it.
//            },

            FieldMd("Filial","Filial","@LiberaPara")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("0","Nenhuma")
                ) },
        ).forEach { userFieldsMDService.findOrCreate(it) }


        listOf(
            comissaoObject()
        ).forEach{
            udoService.findOrCreate(it)
        }

        listOf(
            FieldMd("tipoComissao","Selecionar Comissao","OPLN", DbType.db_Alpha)
                .also { it.linkedUDO = "comissao" },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }


    fun comissaoObject(): UserDefinedObject {
        val ud = UserDefinedObject("comissao", "Comissões", "COMISSAO",)
        ud.UserObjectMD_ChildTables.addAll(listOf(
            ChildTables("CONDICOESFV"),
            ChildTables("LIBERAPARA")
        ))
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Código",0),
            FormColumns("Name","Descrição",0),
            FormColumns("U_porcentagem","comissão em porcentagem",0),
            FormColumns("U_desconto","Desconto (%) do vendedor",0),
            FormColumns("U_regressiva","Comissão regressiva?",0),
            FormColumns("U_desconto","Desconto (%)",1),
            FormColumns("U_juros","Juros (%)",1),
            FormColumns("U_prazo","Prazo",1),
        ))
        ud.setMenu(43541,1)
//        ud.FormSRF = ClassPathResource("udo-view/comissao.xml").file.readText()
        return ud;
    }
}