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
@ConditionalOnProperty(value = ["fields.ord"], havingValue = "true", matchIfMissing = false)
class CarregamentoConfiguration(val userFieldsMDService: UserFieldsMDService,
                                val udoService: UserObjectsMDService,
                                val userKeyService: UserKeyMDService,
                                val  tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd(
                "ORD_CARREGAMENTO","Ordem de Carregamento", TbType.bott_Document
            ),
            TableMd(
                "ORD_CRG_LINHA","Ordem de Carregamento Linha ", TbType.bott_DocumentLines
            ),
        ).forEach{ tableService.findOrCreate(it)}

        listOf(
            FieldMd("nameOrdem","Nome da Ordem","@ORD_CARREGAMENTO", DbType.db_Alpha),
            FieldMd("Status", "Status", "@ORD_CARREGAMENTO")
                .also {
                    it.ValidValuesMD = listOf(
                        ValuesMd("Aberto", "Aberto"),
                        ValuesMd("Fechado", "Fechado"),
                        ValuesMd("Cancelado", "Cancelado")
                    )
                    it.defaultValue = "Aberto"
                },
            FieldMd("pesoTotal","Peso Total","@ORD_CARREGAMENTO", DbType.db_Numeric)
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        listOf(
            FieldMd("orderDocEntry","Pedido","@ORD_CRG_LINHA", null).also {
                it.LinkedSystemObject = LinkedSystemObject.ulOrders
                it.subType = "st_None"
            },
            FieldMd("itemCode","Produto","@ORD_CRG_LINHA").also {
                it.LinkedSystemObject = LinkedSystemObject.ulItems
            },
            FieldMd("description","Descrição","@ORD_CRG_LINHA", DbType.db_Alpha),
            FieldMd("docNumPedido","DocNum Pedido","@ORD_CRG_LINHA", DbType.db_Numeric),
            FieldMd("cardCode","Cliente","@ORD_CRG_LINHA", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulBusinessPartners
            },
            FieldMd("cardName","Nome Cliente","@ORD_CRG_LINHA", DbType.db_Alpha),
            FieldMd("quantidade","Quantidade","@ORD_CRG_LINHA", DbType.db_Numeric),
            FieldMd("pesoItem","Peso Item","@ORD_CRG_LINHA", DbType.db_Numeric),
            FieldMd("unMedida","Unidade de Medida","@ORD_CRG_LINHA", DbType.db_Alpha),
            FieldMd("qtdEstoque","Unidade de Medida","@ORD_CRG_LINHA", DbType.db_Numeric),
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        val udoProperties = getUserDefined()
        udoService.findOrCreate(udoProperties)
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject("ORD_CARREGAMENTO", "Ordem de Carregamento",
            "ORD_CARREGAMENTO",
            UDOObjType.boud_Document,
            YesNo.tNO)
        ud.popChildTable(ChildTables("ORD_CRG_LINHA"))
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("DocEntry","DocEntry",0),
            FormColumns("DocNum","DocNum",0)
        ))

        return ud;
    }
}

