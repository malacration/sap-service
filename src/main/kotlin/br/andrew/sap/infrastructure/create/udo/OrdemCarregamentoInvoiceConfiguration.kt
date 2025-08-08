package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.TableMd
import br.andrew.sap.model.sap.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserKeyMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.ord.invoice"], havingValue = "true", matchIfMissing = false)
class OrdemCarregamentoInvoiceConfiguration(val userFieldsMDService: UserFieldsMDService,
                                            val  tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd(
                "OINV","Nota Fiscal de Saida", TbType.bott_Document
            ),
        ).forEach{ tableService.findOrCreate(it)}

        listOf(
            FieldMd("ordemCarregamento","Ord. Carregamento","OINV", DbType.db_Numeric),
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }
    }
}

