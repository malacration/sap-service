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
 * Provisiona o catálogo de regras de trava como UDO (TRAVA_REGRA), espelhando
 * o COB_DOMINIO. Registrar como UDO (e não UDT solta) é o que faz o Service
 * Layer expor /b1s/v1/TRAVA_REGRA. A chave da regra é o Code nativo do UDO.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.trava"], havingValue = "true", matchIfMissing = false)
class TravaRegraConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(
            TableMd("TRAVA_REGRA", "Trava - Regra", TbType.bott_MasterData)
        )

        listOf(
            FieldMd("Descricao", "Descrição", "@TRAVA_REGRA", DbType.db_Alpha).also { it.size = 100 },
            FieldMd("Ordem", "Ordem", "@TRAVA_REGRA", DbType.db_Numeric),
            FieldMd("Ativo", "Ativo", "@TRAVA_REGRA", DbType.db_Alpha).also { it.defaultValue = "Y" },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(
            UserDefinedObject("TRAVA_REGRA", "Trava - Regra", "TRAVA_REGRA", UDOObjType.boud_MasterData)
        )
    }
}
