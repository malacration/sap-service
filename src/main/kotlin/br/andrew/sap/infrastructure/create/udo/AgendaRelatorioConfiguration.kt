package br.andrew.sap.infrastructure.create.udo

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
 * Provisiona a agenda de envio de relatorios por e-mail como UDO (AGENDA_RELATORIO). Sem a flag, o
 * AgendaRelatorioService cai na agenda padrao (segunda, 07:00, sem copia), como era antes da tela.
 * Campo novo em UDO ja existente pede reiniciar o Service Layer.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.agenda-relatorio"], havingValue = "true", matchIfMissing = false)
class AgendaRelatorioConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(
            TableMd("AGENDA_RELATORIO", "Agenda de relatorio", TbType.bott_MasterData)
        )

        listOf(
            FieldMd("Ativo", "Ativo", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 1; it.defaultValue = "Y" },
            FieldMd("Dias", "Dias da semana", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 20 },
            FieldMd("Hora", "Hora", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 5 },
            FieldMd("Copia", "E-mails em copia", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 254 },
            FieldMd("UltimoEnvio", "Ultimo envio", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 25 },
            FieldMd("UltimoStatus", "Ultimo status", "@AGENDA_RELATORIO", DbType.db_Alpha).also { it.size = 254 },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(
            UserDefinedObject("AGENDA_RELATORIO", "Agenda de relatorio", "AGENDA_RELATORIO", UDOObjType.boud_MasterData)
        )
    }
}
