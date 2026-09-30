package br.andrew.sap.infrastructure.create.udo

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
 * Provisiona o UDO ACESSO_REGRAS, onde cada linha e uma versao completa das regras de acesso.
 * Segue o TRAVA_REGRA: registrar como UDO (e nao UDT solta) e o que faz o Service Layer expor
 * /b1s/v1/ACESSO_REGRAS.
 *
 * Duas configuracoes fogem do padrao de proposito:
 *  - CanDelete = NO: apagar a linha mais nova deixaria a proxima gravacao reaproveitar o Code, e o
 *    Code e o que serializa gravacoes simultaneas.
 *  - CanLog = NO: com o log ligado o SAP copia o memo inteiro a cada gravacao para a tabela de log.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.acesso"], havingValue = "true", matchIfMissing = false)
class AcessoRegrasConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(
            TableMd("ACESSO_REGRAS", "Acesso - Regras", TbType.bott_MasterData)
        )

        listOf(
            FieldMd("Documento", "Documento das regras", "@ACESSO_REGRAS", DbType.db_Memo),
            FieldMd("IdEscrita", "Id da gravação", "@ACESSO_REGRAS", DbType.db_Alpha).also { it.size = 50 },
            FieldMd("Usuario", "Usuário", "@ACESSO_REGRAS", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("UsuarioId", "Id do usuário", "@ACESSO_REGRAS", DbType.db_Alpha).also { it.size = 50 },
            FieldMd("Data", "Data", "@ACESSO_REGRAS", DbType.db_Date),
            FieldMd("Hora", "Hora", "@ACESSO_REGRAS", DbType.db_Alpha).also { it.size = 8 },
            FieldMd("Origem", "Origem", "@ACESSO_REGRAS", DbType.db_Alpha).also { it.size = 20 },
            FieldMd("Comentario", "Comentário", "@ACESSO_REGRAS", DbType.db_Alpha),
            FieldMd("Resumo", "Resumo da mudança", "@ACESSO_REGRAS", DbType.db_Alpha),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(
            UserDefinedObject(
                "ACESSO_REGRAS", "Acesso - Regras", "ACESSO_REGRAS", UDOObjType.boud_MasterData,
                CanDelete = YesNo.tNO,
                CanLog = YesNo.tNO,
            )
        )
    }
}
