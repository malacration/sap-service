package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.entity.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.TableMd
import br.andrew.sap.model.sap.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserKeyMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.calc"], havingValue = "true", matchIfMissing = false)
class CalculadoraPrecoConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val userKeyService: UserKeyMDService,
    val  tableService: UserTablesMDService
) {

    init {
        tableService.findOrCreate(TableMd(
            "AR_CALC_PRECO","Calculadora Preço", TbType.bott_Document
        ))

        listOf(
            FieldMd("relatorioJson","Relatorio","@AR_CALC_PRECO", DbType.db_Memo),
            FieldMd("descricao","Descrição","@AR_CALC_PRECO", DbType.db_Alpha),
            FieldMd("lastUserId","Last User Id","@AR_CALC_PRECO", DbType.db_Alpha),
            FieldMd("lastUserOrigin","Origem do Login","@AR_CALC_PRECO", DbType.db_Alpha),
            FieldMd("lastUserName","Origem do Login","@AR_CALC_PRECO", DbType.db_Alpha)
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        val udoProperties = getUserDefined()
        udoService.findOrCreate(udoProperties)
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject("AR_CALC_PRECO", "Contrato Venda Futura",
            "AR_CALC_PRECO",
            UDOObjType.boud_Document,
            YesNo.tNO)
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("DocEntry","DocEntry",0),
            FormColumns("DocNum","DocNum",0)
        ))

        ObjectMapper().registerModule(KotlinModule()).writeValueAsString(ud)
        return ud;
    }
}

