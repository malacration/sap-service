package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.model.entity.*
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ComissaoConfiguration(val userFieldsMDService: UserFieldsMDService,
                            val udoService: UserObjectsMDService,
                            val  tableService: UserTablesMDService) {
    init {
        listOf(
            TableMd(
                "COMISSAO","Tabela com regras de comissao", TbType.bott_MasterData
            ),
            TableMd(
                "condicoesFV","Linha condiçoes ", TbType.bott_MasterDataLines
            ),
            TableMd(
                "LiberaPara","Libera para", TbType.bott_MasterDataLines
            )
        ).forEach{ tableService.findOrCreate(it)}

        listOf(
            FieldMd("porcentagem","comissão em porcentagem","@COMISSAO", DbType.db_Float),
            FieldMd("desconto","Desconto (%) do vendedor","@COMISSAO", DbType.db_Float),
            FieldMd("regressiva","Comissão regressiva?","@COMISSAO")
                .also {
                    it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                    it.defaultValue = "0"
                },
            FieldMd("desconto","Desconto (%)","@condicoesFV", DbType.db_Float),
            FieldMd("juros","Juros (%)","@condicoesFV", DbType.db_Float),
            //SAP nao tem um LinkedSystemObject pra OCTG (condicao de pagamento) - o campo
            //fica numerico mesmo, guardando o OCTG.GroupNum direto (mesma convencao ja usada
            //pelo sap-sql: @CONDICOESFV.U_prazo comparado direto com OCTG.GroupNum)
            FieldMd("prazo","Prazo (GroupNum)","@condicoesFV", DbType.db_Numeric),
            FieldMd("Filial","Filial","@LiberaPara")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("0","Nenhuma")
                ) },
            FieldMd("vendedor","Vendedor","@LiberaPara", DbType.db_Alpha),
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
        ud.popChildTable(ChildTables("CONDICOESFV"),ChildTables("LIBERAPARA"))
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Código",0,ud),
            FormColumns("Name","Descrição",0,ud),
            FormColumns("U_porcentagem","comissão em porcentagem",0,ud),
            FormColumns("U_desconto","Desconto (%) do vendedor",0,ud),
            FormColumns("U_regressiva","Comissão regressiva?",0,ud),
            FormColumns("U_desconto","Desconto (%)",1,ud),
            FormColumns("U_juros","Juros (%)",1,ud),
            FormColumns("U_prazo","Prazo",1,ud),
        ))
        ud.setMenu(43541,1)
//        ud.FormSRF = ClassPathResource("udo-view/comissao.xml").file.readText()
        return ud;
    }
}