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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
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
                "AR_CF_LINHA","Linha condiçoes ", TbType.bott_DocumentLines
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
        ud.popChildTable(ChildTables("AR_CF_LINHA",ud))
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Codigo",0),
            FormColumns("U_orderDocEntry","Pedido Origem",0),
            FormColumns("U_itemCode","Item code",1),
        ))
        ud.setMenu(43541,1)
        ud.FormSRF = "<?xml version=\"1.0\" encoding=\"utf-16\"?><Application><forms><action type=\"add\"><form appformnumber=\"-1\" FormType=\"UDO_FT_AR_CONTRATO_FUTURO\" type=\"0\" BorderStyle=\"0\" uid=\"UDO_F_AR_CONTRATO_FUTURO\" title=\"Contrato Venda Futura\" visible=\"1\" default_button=\"\" mode=\"3\" pane=\"1\" color=\"0\" left=\"370\" top=\"127\" width=\"600\" height=\"392\" AutoManaged=\"1\" SupportedModes=\"15\" ObjectType=\"AR_CONTRATO_FUTURO\"><datasources><dbdatasources><action type=\"add\"><datasource tablename=\"@AR_CONTRATO_FUTURO\" /><datasource tablename=\"@AR_CF_LINHA\" /></action></dbdatasources><userdatasources><action type=\"add\"><datasource uid=\"FolderDS\" type=\"9\" size=\"10\" /></action></userdatasources></datasources><Menus /><items><action type=\"add\"><item uid=\"0_U_S\" type=\"8\" left=\"6\" tab_order=\"0\" width=\"121\" top=\"6\" height=\"14\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"DocEntry\" linkto=\"0_U_E\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"DocEntry\" /></item><item uid=\"0_U_E\" type=\"16\" left=\"127\" tab_order=\"0\" width=\"148\" top=\"6\" height=\"14\" visible=\"1\" enabled=\"0\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"DocEntry\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific TabOrder=\"24\"><databind databound=\"1\" table=\"@AR_CONTRATO_FUTURO\" alias=\"DocEntry\" /></specific></item><item uid=\"20_U_S\" type=\"8\" left=\"306\" tab_order=\"0\" width=\"121\" top=\"6\" height=\"14\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Pedido\" linkto=\"20_U_E\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Pedido\" /></item><item uid=\"20_U_E\" type=\"16\" left=\"427\" tab_order=\"0\" width=\"148\" top=\"6\" height=\"14\" visible=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Pedido\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific TabOrder=\"25\"><databind databound=\"1\" table=\"@AR_CONTRATO_FUTURO\" alias=\"U_orderDocEntry\" /></specific></item><item uid=\"21_U_S\" type=\"8\" left=\"6\" tab_order=\"0\" width=\"121\" top=\"21\" height=\"14\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Cliente\" linkto=\"21_U_E\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Cliente\" /></item><item uid=\"21_U_E\" type=\"16\" left=\"127\" tab_order=\"0\" width=\"148\" top=\"21\" height=\"14\" visible=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Cliente\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific TabOrder=\"26\"><databind databound=\"1\" table=\"@AR_CONTRATO_FUTURO\" alias=\"U_cardCode\" /></specific></item><item uid=\"22_U_S\" type=\"8\" left=\"306\" tab_order=\"0\" width=\"121\" top=\"21\" height=\"14\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Data de Criação\" linkto=\"22_U_E\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Data de Criação\" /></item><item uid=\"22_U_E\" type=\"16\" left=\"427\" tab_order=\"0\" width=\"148\" top=\"21\" height=\"14\" visible=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Data de Criação\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific TabOrder=\"27\"><databind databound=\"1\" table=\"@AR_CONTRATO_FUTURO\" alias=\"U_dataCriacao\" /></specific></item><item uid=\"23_U_S\" type=\"8\" left=\"6\" tab_order=\"0\" width=\"121\" top=\"36\" height=\"14\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Valor Frete\" linkto=\"23_U_E\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Valor Frete\" /></item><item uid=\"23_U_E\" type=\"16\" left=\"127\" tab_order=\"0\" width=\"148\" top=\"36\" height=\"14\" visible=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" description=\"Valor Frete\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific TabOrder=\"28\"><databind databound=\"1\" table=\"@AR_CONTRATO_FUTURO\" alias=\"U_valorFrete\" /></specific></item><item uid=\"1\" type=\"4\" left=\"6\" tab_order=\"\" width=\"65\" top=\"332\" height=\"19\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"OK\" /></item><item uid=\"2\" type=\"4\" left=\"76\" tab_order=\"\" width=\"65\" top=\"332\" height=\"19\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Cancelar\" /></item><item uid=\"0_U_FD\" type=\"99\" left=\"6\" tab_order=\"\" width=\"116\" top=\"88\" height=\"19\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific caption=\"Linha condiçoes\" AffectsFormMode=\"1\" val_on=\"Y\" val_off=\"N\" pane=\"1\" AutoPaneSelection=\"1\"><databind databound=\"1\" table=\"\" alias=\"FolderDS\" /></specific></item><item uid=\"U_RC\" type=\"100\" left=\"5\" tab_order=\"\" width=\"570\" top=\"106\" height=\"207\" visible=\"1\" enabled=\"1\" from_pane=\"0\" to_pane=\"0\" disp_desc=\"\" right_just=\"0\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /></item><item uid=\"0_U_G\" type=\"127\" left=\"15\" tab_order=\"\" width=\"555\" top=\"133\" height=\"163\" visible=\"1\" enabled=\"1\" from_pane=\"1\" to_pane=\"1\" disp_desc=\"\" right_just=\"0\" linkto=\"\" forecolor=\"-1\" backcolor=\"-1\" text_style=\"0\" font_size=\"-1\" supp_zeros=\"0\" AffectsFormMode=\"1\" IsAutoGenerated=\"1\"><AutoManagedAttribute /><specific SelectionMode=\"2\"><columns><action type=\"add\"><column uid=\"#\" type=\"16\" title=\"#\" description=\"#\" visible=\"1\" AffectsFormMode=\"1\" width=\"20\" disp_desc=\"0\" editable=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\" /><column uid=\"C_0_1\" type=\"16\" title=\"Produto\" description=\"Produto\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_itemCode\" /></column><column uid=\"C_0_2\" type=\"16\" title=\"Valor Negociado\" description=\"Valor Negociado\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_precoNegociado\" /></column><column uid=\"C_0_3\" type=\"16\" title=\"Quantidade\" description=\"Quantidade\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_quantity\" /></column><column uid=\"C_0_4\" type=\"16\" title=\"Preço base\" description=\"Preço base\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_precoBase\" /></column><column uid=\"C_0_5\" type=\"16\" title=\"desconto vendedor\" description=\"desconto vendedor\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_desconto\" /></column><column uid=\"C_0_6\" type=\"16\" title=\"comissão\" description=\"comissão\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_comissao\" /></column><column uid=\"C_0_7\" type=\"16\" title=\"Unidade Medida\" description=\"Unidade Medida\" visible=\"1\" AffectsFormMode=\"1\" width=\"100\" disp_desc=\"0\" right_just=\"0\" val_on=\"Y\" val_off=\"N\" backcolor=\"-1\" forecolor=\"-1\" text_style=\"0\" font_size=\"-1\" IsAutoGenerated=\"1\"><databind databound=\"1\" table=\"@AR_CF_LINHA\" alias=\"U_MeasureUnit\" /></column></action></columns></specific></item></action></items><ChooseFromListCollection /><DataBrowser BrowseBy=\"0_U_E\" /><Settings Enabled=\"1\" EnableRowFormat=\"1\" /><items><action type=\"group\"><item uid=\"0_U_FD\" IsAutoGenerated=\"0\" /></action></items></form></action></forms></Application>"
        return ud;
    }
}

