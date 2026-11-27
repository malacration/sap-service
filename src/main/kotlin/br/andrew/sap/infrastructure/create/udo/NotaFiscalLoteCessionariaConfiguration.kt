package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UDOObjType
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Cessionarias (securitizadoras) do "Comunicado de Cessao de Titulo" do download em lote de NF.
 * Code = CNPJ so com digitos, Name = razao social. O UDO existe para a tabela poder ser
 * preenchida pela tela do SAP (master data sem UDO nao abre cadastro).
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.nota-fiscal-lote-cessionaria"], havingValue = "true", matchIfMissing = false)
class NotaFiscalLoteCessionariaConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(TableMd("NFL_CESSIONARIA", "Lote NF - Cessionaria", TbType.bott_MasterData))

        listOf(
            FieldMd("Cnpj", "CNPJ", "@NFL_CESSIONARIA", DbType.db_Alpha).also { it.size = 18 },
            FieldMd("Telefone", "Telefone", "@NFL_CESSIONARIA", DbType.db_Alpha).also { it.size = 20 },
            FieldMd("NomePdf", "Nome no PDF", "@NFL_CESSIONARIA", DbType.db_Alpha).also { it.size = 254 },
            FieldMd("Ativo", "Ativo", "@NFL_CESSIONARIA", DbType.db_Alpha).also {
                it.size = 1
                it.defaultValue = "Y"
                it.ValidValuesMD = listOf(ValuesMd("Y", "Sim"), ValuesMd("N", "Nao"))
            },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(
            UserDefinedObject("NFL_CESSIONARIA", "Lote NF - Cessionaria", "NFL_CESSIONARIA", UDOObjType.boud_MasterData)
        )
    }
}
