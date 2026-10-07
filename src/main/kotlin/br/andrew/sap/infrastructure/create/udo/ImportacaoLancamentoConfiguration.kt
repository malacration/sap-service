package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.DbSubType
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UDOObjType
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Provisiona o log das importacoes de lancamentos (UDO LC_IMPORTACAO), gravado pela tela de
 * importacao no mesmo changeset dos lancamentos (ver ImportacaoLancamentoService).
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.importacao-lancamento"], havingValue = "true", matchIfMissing = false)
class ImportacaoLancamentoConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(
            TableMd("LC_IMPORTACAO", "Importação de Lançamentos", TbType.bott_MasterData)
        )

        listOf(
            FieldMd("Usuario", "Usuário", "@LC_IMPORTACAO", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("UsuarioId", "ID do Usuário", "@LC_IMPORTACAO", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("Data", "Data", "@LC_IMPORTACAO", DbType.db_Date),
            FieldMd("Hora", "Hora", "@LC_IMPORTACAO", DbType.db_Alpha).also { it.size = 5 },
            FieldMd("Arquivo", "Arquivo", "@LC_IMPORTACAO", DbType.db_Alpha),
            //SHA-256 em hexadecimal
            FieldMd("Hash", "Hash do Arquivo", "@LC_IMPORTACAO", DbType.db_Alpha).also { it.size = 64 },
            //mesmo motivo do DocEntry em CobrancaConfiguration: sem size o SAP cria SMALLINT
            FieldMd("Linhas", "Linhas", "@LC_IMPORTACAO", DbType.db_Numeric).also {
                it.size = 11
                it.editSize = 11
            },
            FieldMd("Total", "Valor Total", "@LC_IMPORTACAO", DbType.db_Float).also { it.subType = DbSubType.st_Sum },
            FieldMd("Forcado", "Reimportação Forçada", "@LC_IMPORTACAO", DbType.db_Alpha).also { it.size = 1 },
            FieldMd("Lancamentos", "Lançamentos", "@LC_IMPORTACAO", DbType.db_Memo),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        //CanDelete=tNO: e trilha de auditoria. CanLog=tNO: senao o SAP copia cada linha para a
        //tabela de log dele, inclusive o memo com os lancamentos.
        udoService.findOrCreate(
            UserDefinedObject(
                "LC_IMPORTACAO", "Importação de Lançamentos", "LC_IMPORTACAO", UDOObjType.boud_MasterData,
                CanDelete = YesNo.tNO,
                CanLog = YesNo.tNO,
            )
        )
    }
}
