package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
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
@ConditionalOnProperty(value = ["fields.vf"], havingValue = "true", matchIfMissing = false)
class ContratoVendaFuturaConfiguration(val userFieldsMDService: UserFieldsMDService,
                                       val udoService: UserObjectsMDService,
                                       val userKeyService: UserKeyMDService,
                                       val  tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd(
                "AR_CONTRATO_FUTURO","Contratos venda futura", TbType.bott_Document
            ),
            TableMd(
                "AR_CF_LINHA","Contrato Linha ", TbType.bott_DocumentLines
            ),
        ).forEach{ tableService.findOrCreate(it)}

        listOf(
            FieldMd("orderDocEntry","Pedido","@AR_CONTRATO_FUTURO", null).also {
                it.LinkedSystemObject = LinkedSystemObject.ulOrders
                it.subType = "st_None"
            },
            FieldMd("cardCode","Cliente","@AR_CONTRATO_FUTURO", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulBusinessPartners
            },
            FieldMd("cardName","Nome Cliente","@AR_CONTRATO_FUTURO", DbType.db_Alpha),
            FieldMd("dataCriacao","Data de Criação","@AR_CONTRATO_FUTURO", DbType.db_Date),
            FieldMd("valorFrete","Valor Frete","@AR_CONTRATO_FUTURO", DbType.db_Float),
            FieldMd("filial","Filial","@AR_CONTRATO_FUTURO", DbType.db_Numeric),
            FieldMd("vendedor","Vendedor","@AR_CONTRATO_FUTURO", DbType.db_Float).also {
                //TODO um dia ver se existe vendedor nesse vinculo
                it.LinkedSystemObject = LinkedSystemObject.ulInventoryGenExit
            },
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        listOf(
            FieldMd("itemCode","Produto","@AR_CF_LINHA").also {
                it.LinkedSystemObject = LinkedSystemObject.ulItems
            },
            FieldMd("description","Descrição","@AR_CF_LINHA", DbType.db_Alpha),
            FieldMd("precoNegociado","Valor Negociado","@AR_CF_LINHA", DbType.db_Float),
            FieldMd("quantity","Quantidade","@AR_CF_LINHA", DbType.db_Float),
            FieldMd("precoBase","Preço base","@AR_CF_LINHA", DbType.db_Float),
            FieldMd("desconto","desconto vendedor","@AR_CF_LINHA", DbType.db_Float),
            FieldMd("comissao","comissão","@AR_CF_LINHA", DbType.db_Float),
            FieldMd("MeasureUnit","Unidade Medida","@AR_CF_LINHA", DbType.db_Alpha),
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        val udoProperties = getUserDefined()
        udoService.findOrCreate(udoProperties)

        listOf(
            UserKeyMD("ukOrder","@AR_CONTRATO_FUTURO",YesNo.tYES,listOf(Elements("orderDocEntry"))),
        ).forEach { userKeyService.findOrCreate(it) }


        listOf(
            FieldMd("venda_futura","Contrato Venda Futura","OINV", DbType.db_Alpha)
                .also { it.linkedUDO = udoProperties.Code },
        ).forEach { userFieldsMDService.findOrCreate(it) }


    }

    fun delete(ud : UserDefinedObject) : OData? {
        val predicates = mutableListOf(
            Predicate("Code",ud.Code, Condicao.EQUAL),
        )
        val result = udoService.get(Filter(predicates))
        if(!result.tryGetValues<UserDefinedObject>().isEmpty()){
            println("Excluindo code objet")
            return udoService.delete("'${ud.Code}'")
        }
        return null
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject("AR_CONTRATO_FUTURO", "Contrato Venda Futura",
            "AR_CONTRATO_FUTURO",
            UDOObjType.boud_Document,
            YesNo.tNO)
        ud.popChildTable(ChildTables("AR_CF_LINHA"))
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("DocEntry","DocEntry",0),
            FormColumns("DocNum","DocNum",0)
        ))

//        ud.UserObjectMD_FindColumns.addAll(listOf(
//            FindColumns("CreateDate","CreateDate"),
//            FindColumns("UpdateDate","UpdateDate"),
//            FindColumns("U_orderDocEntry","Pedido"),
//            FindColumns("U_cardCode","Cliente"),
//            FindColumns("U_dataCriacao","Data de Criação"),
//            FindColumns("U_valorFrete","Valor Frete"),
//        ))

        ObjectMapper().registerModule(KotlinModule()).writeValueAsString(ud)
        return ud;
    }
}

