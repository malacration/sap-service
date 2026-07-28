package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.Elements
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.LinkedSystemObject
import br.andrew.sap.model.entity.UDOObjType
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.entity.UserKeyMD
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
@ConditionalOnProperty(value = ["fields.cobranca"], havingValue = "true", matchIfMissing = false)
class CobrancaConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val userKeyService: UserKeyMDService,
    val tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd("COB_TITULO", "Cobrança - Título", TbType.bott_MasterData),
            TableMd("COB_TITULO_L", "Cobrança - Histórico", TbType.bott_MasterDataLines),
            TableMd("COB_DOMINIO", "Cobrança - Domínio", TbType.bott_MasterData),
        ).forEach { tableService.findOrCreate(it) }

        listOf(
            // "NF" (fatura, OINV) ou "AD" (adiantamento, ODPI) - OBRIGATÓRIO na chave: o
            // DocEntry de fatura e de adiantamento vêm de contadores independentes no SAP
            // (ObjType 13 x 203), então sem esse campo uma fatura e um adiantamento com o
            // mesmo DocEntry+Parcela colidiriam e sobrescreveriam um ao outro.
            FieldMd("Tipo", "Tipo (NF/AD)", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("DocEntry", "Nº Doc.", "@COB_TITULO", DbType.db_Numeric),
            FieldMd("InstlmntID", "Nº Parcela", "@COB_TITULO", DbType.db_Numeric),
            FieldMd("CardCode", "Cliente", "@COB_TITULO", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulBusinessPartners
            },
            FieldMd("Status", "Status", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("Acao", "Ação de Cobrança", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("Situacao", "Situação", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("Ocorrencia", "Ocorrência", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("Observacao", "Observação", "@COB_TITULO", DbType.db_Memo),
            FieldMd("Cobrador", "Cobrador", "@COB_TITULO", DbType.db_Alpha),
            FieldMd("DataAcao", "Data da Ação", "@COB_TITULO", DbType.db_Date),
            FieldMd("DataPromessa", "Data Prometida", "@COB_TITULO", DbType.db_Date),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("Data", "Data", "@COB_TITULO_L", DbType.db_Date),
            // Tabela de linha (bott_MasterDataLines) nao ganha CreateDate/CreateTime do SAP
            // como a master ganha - confirmado direto no banco (so tem LineId/Object/LogInst
            // + os campos U_*). Sem um campo proprio pra hora, so da pra saber o dia da acao.
            FieldMd("Hora", "Hora", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Usuario", "Usuário", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Status", "Status", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Acao", "Ação de Cobrança", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Situacao", "Situação", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Ocorrencia", "Ocorrência", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Observacao", "Observação", "@COB_TITULO_L", DbType.db_Memo),
            FieldMd("Cobrador", "Cobrador", "@COB_TITULO_L", DbType.db_Alpha),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("Tipo", "Tipo", "@COB_DOMINIO", DbType.db_Alpha),
            FieldMd("Codigo", "Código", "@COB_DOMINIO", DbType.db_Alpha),
            FieldMd("Descricao", "Descrição", "@COB_DOMINIO", DbType.db_Alpha),
            FieldMd("Ordem", "Ordem", "@COB_DOMINIO", DbType.db_Numeric),
            FieldMd("Ativo", "Ativo", "@COB_DOMINIO", DbType.db_Alpha).also {
                it.defaultValue = "Y"
            },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        val cobTitulo = UserDefinedObject(
            "COB_TITULO", "Cobrança - Título", "COB_TITULO", UDOObjType.boud_MasterData
        )
        cobTitulo.popChildTable(ChildTables("COB_TITULO_L"))
        udoService.findOrCreate(cobTitulo)

        val cobDominio = UserDefinedObject(
            "COB_DOMINIO", "Cobrança - Domínio", "COB_DOMINIO", UDOObjType.boud_MasterData
        )
        udoService.findOrCreate(cobDominio)

        // A key antiga ("ukCobTit") so tinha (DocEntry, InstlmntID) - sem o Tipo ela
        // bloquearia uma fatura e um adiantamento genuinamente diferentes que calhem de
        // ter o mesmo DocEntry+Parcela. O formato certo pra apagar essa key via Service
        // Layer nao foi encontrado (nem TableName+KeyName nem KeyName sozinho funcionaram),
        // entao ela fica para trás - se isso incomodar, remova manualmente pelo SAP B1
        // (Ferramentas > Personalização > Chaves de Tabelas Definidas pelo Usuário).
        listOf(
            UserKeyMD(
                // KeyName tem limite curto no SAP (visto em produção: 11 chars já é "Value too long")
                "ukCobTit2", "@COB_TITULO", YesNo.tYES,
                listOf(Elements("Tipo"), Elements("DocEntry"), Elements("InstlmntID"))
            ),
        ).forEach { userKeyService.findOrCreate(it) }
    }
}
