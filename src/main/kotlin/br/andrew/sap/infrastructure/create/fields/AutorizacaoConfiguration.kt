package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.model.entity.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserObjectsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

//@AUTORIZACAO: um registro por documento retido pendente de autorizacao (payload
//completo em U_payload, reenviado ao SAP no fluxo direto de sempre quando aprovado).
//@AUTORIZADOR: tabela de roteamento motivo -> usuario, independente (nao e filha de
//AUTORIZACAO), mesmo espirito do LiberaPara da Comissao.
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class AutorizacaoConfiguration(val userFieldsMDService: UserFieldsMDService,
                                val udoService: UserObjectsMDService,
                                val tableService: UserTablesMDService) {
    init {
        listOf(
            //TableDescription do SAP tem limite de 30 caracteres (UserTablesMD)
            TableMd("AUTORIZACAO","Autorizacao de documentos", TbType.bott_MasterData),
            TableMd("AUTORIZADOR","Autorizadores por motivo", TbType.bott_MasterData)
        ).forEach{ tableService.findOrCreate(it) }

        listOf(
            FieldMd("tipoDocumento","Tipo de Documento","@AUTORIZACAO")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("COTACAO","Cotação"),
                    ValuesMd("PEDIDO_VENDA","Pedido de Venda"),
                ) },
            //motivo fica livre (sem ValidValuesMD fechado) de proposito, pra caber
            //motivo novo (proxima regra do motor de regras) sem alterar o UDF
            FieldMd("motivo","Motivo","@AUTORIZACAO", DbType.db_Alpha),
            FieldMd("cardCode","Código do Cliente","@AUTORIZACAO", DbType.db_Alpha),
            FieldMd("cardName","Nome do Cliente","@AUTORIZACAO", DbType.db_Alpha),
            FieldMd("valor","Valor do Documento","@AUTORIZACAO", DbType.db_Float),
            //documento original serializado (JSON) - o que e reenviado ao SAP no
            //fluxo direto quando aprovado. mesmo padrao de U_relatorioJson em
            //CalculadoraPrecoConfiguration.kt (AR_CALC_PRECO)
            FieldMd("payload","Documento (JSON)","@AUTORIZACAO", DbType.db_Memo),
            FieldMd("status","Status","@AUTORIZACAO")
                .also {
                    it.ValidValuesMD = listOf(
                        ValuesMd("PENDENTE","Pendente"),
                        ValuesMd("APROVADO","Aprovado"),
                        ValuesMd("REJEITADO","Rejeitado"),
                    )
                    it.defaultValue = "PENDENTE"
                },
            FieldMd("solicitante","Solicitante","@AUTORIZACAO", DbType.db_Alpha),
            FieldMd("autorizador","Autorizador","@AUTORIZACAO", DbType.db_Alpha),
            FieldMd("observacao","Observação","@AUTORIZACAO", DbType.db_Memo),
            //preenchido so depois de aprovado e criado de verdade no SAP, pra rastreabilidade
            FieldMd("docEntryCriado","DocEntry Criado","@AUTORIZACAO", DbType.db_Numeric),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("motivo","Motivo","@AUTORIZADOR", DbType.db_Alpha),
            //username da aplicacao (Keycloak/role.bind), nao UserSign do SAP - a
            //aprovacao acontece dentro do front-sap, nao dentro do SAP
            FieldMd("usuario","Usuário","@AUTORIZADOR", DbType.db_Alpha),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            autorizacaoObject(),
            autorizadorObject()
        ).forEach{
            udoService.findOrCreate(it)
        }
    }

    //ManageSeries = Y: o Code e auto-numerado pelo SAP - nao ha um "codigo" com
    //sentido de negocio pra um pedido de autorizacao gerado pela propria aplicacao
    //(diferente de Regiao/Comissao, cujo Code e digitado por um usuario)
    fun autorizacaoObject(): UserDefinedObject {
        val ud = UserDefinedObject("autorizacao", "Autorizações", "AUTORIZACAO",
            ManageSeries = YesNo.tYES)
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Código",0,ud),
            FormColumns("Name","Descrição",0,ud),
            FormColumns("U_tipoDocumento","Tipo de Documento",0,ud),
            FormColumns("U_motivo","Motivo",0,ud),
            FormColumns("U_status","Status",0,ud),
        ))
        return ud
    }

    fun autorizadorObject(): UserDefinedObject {
        val ud = UserDefinedObject("autorizador", "Autorizadores", "AUTORIZADOR",
            ManageSeries = YesNo.tYES)
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code","Código",0,ud),
            FormColumns("Name","Descrição",0,ud),
            FormColumns("U_motivo","Motivo",0,ud),
            FormColumns("U_usuario","Usuário",0,ud),
        ))
        return ud
    }
}
