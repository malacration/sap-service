package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.DbSubType
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UDOObjType
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Lancamento de Retorno (LCM do desconto de duplicatas): o interruptor do lancamento automatico (LANC_RET_CFG, uma linha so,
 * Code AUTOMATICO) e o historico do que foi lancado, recusado ou ligado/desligado (LANC_RET_LOG).
 * No SAP, e nao em properties, para o financeiro ligar e desligar pela tela sem deploy, e para o
 * historico acompanhar a base. Registrados como UDO para o Service Layer expor os dois.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.lancamento-retorno"], havingValue = "true", matchIfMissing = false)
class LancamentoRetornoConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(TableMd("LANC_RET_CFG", "Lanc. Retorno - Config", TbType.bott_MasterData))
        tableService.findOrCreate(TableMd("LANC_RET_LOG", "Lanc. Retorno - Histórico", TbType.bott_MasterData))

        listOf(
            FieldMd("Ativo", "Ativo", "@LANC_RET_CFG", DbType.db_Alpha).also { it.size = 1; it.defaultValue = "N" },
            FieldMd("Usuario", "Alterado por", "@LANC_RET_CFG", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("Data", "Alterado em", "@LANC_RET_CFG", DbType.db_Alpha).also { it.size = 19 },

            //Numeric sem size vira SMALLINT no HANA: retorno e LCM passam de 32767. O SL recusa size 11
            //sem editSize 11 ("Field size deviates from legal range"); ver CobrancaConfiguration
            FieldMd("Retorno", "Retorno BankPlus", "@LANC_RET_LOG", DbType.db_Numeric).also { it.size = 11; it.editSize = 11 },
            FieldMd("Origem", "Origem", "@LANC_RET_LOG", DbType.db_Alpha).also { it.size = 20 },
            FieldMd("Resultado", "Resultado", "@LANC_RET_LOG", DbType.db_Alpha).also { it.size = 20 },
            FieldMd("Lcm", "LCM", "@LANC_RET_LOG", DbType.db_Numeric).also { it.size = 11; it.editSize = 11 },
            FieldMd("Valor", "Valor", "@LANC_RET_LOG", DbType.db_Float).also { it.subType = DbSubType.st_Sum },
            FieldMd("Mensagem", "Mensagem", "@LANC_RET_LOG", DbType.db_Alpha).also { it.size = 254 },
            FieldMd("Usuario", "Usuário", "@LANC_RET_LOG", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("Data", "Data e hora", "@LANC_RET_LOG", DbType.db_Alpha).also { it.size = 19 },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(
            UserDefinedObject("LANC_RET_CFG", "Lanc. Retorno - Config", "LANC_RET_CFG", UDOObjType.boud_MasterData)
        )
        udoService.findOrCreate(
            UserDefinedObject("LANC_RET_LOG", "Lanc. Retorno - Histórico", "LANC_RET_LOG", UDOObjType.boud_MasterData)
        )
    }
}
