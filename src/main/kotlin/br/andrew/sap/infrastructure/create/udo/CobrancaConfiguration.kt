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
import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
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

    companion object {
        // Campo numerico que guarda DocEntry precisa desse tamanho pra o SAP criar INTEGER em vez
        // de SMALLINT. Ver o comentario no FieldMd("DocEntry") abaixo.
        const val TAMANHO_DOC_ENTRY = 11
    }

    init {
        listOf(
            TableMd("COB_TITULO", "Cobrança - Título", TbType.bott_MasterData),
            TableMd("COB_TITULO_L", "Cobrança - Histórico", TbType.bott_MasterDataLines),
            TableMd("COB_DOMINIO", "Cobrança - Domínio", TbType.bott_MasterData),
            TableMd("COB_TITULO_LOG", "Cobrança - Log de Remoção", TbType.bott_MasterData),
        ).forEach { tableService.findOrCreate(it) }

        listOf(
            FieldMd("Tipo", "Tipo (NF/AD)", "@COB_TITULO", DbType.db_Alpha),
            // Size explicito porque db_Numeric nasce com size null (DbType.db_Numeric), o
            // @JsonInclude(NON_EMPTY) do FieldMd omite o campo do JSON e o SAP aplica o default
            // dele: SizeID 6, que no HANA vira SMALLINT (teto 32767). DocEntry de OINV nesta base
            // ja passa de 150 mil - e a Service Layer NAO reclama, aceita o POST e grava o campo
            // nulo. Como a view de titulos junta a UDT por U_DocEntry, o titulo virava orfao e a
            // tela mostrava "1 - NAO INICIADO" mesmo com o historico gravado.
            // 11 e o tamanho que produz INTEGER: medido no CUFD depois de corrigir o campo em
            // producao pelo cliente do SAP (SizeID/EditSize 11 -> INTEGER(10)).
            FieldMd("DocEntry", "Nº Doc.", "@COB_TITULO", DbType.db_Numeric).also {
                it.size = TAMANHO_DOC_ENTRY
                it.editSize = TAMANHO_DOC_ENTRY
            },
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
            FieldMd("Hora", "Hora", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Usuario", "Usuário", "@COB_TITULO_L", DbType.db_Alpha),
            // Identidade de quem registrou. O nome (Usuario) e so exibicao: homonimo apagaria a
            // linha do outro e quem fosse renomeado no SAP perderia acesso as proprias linhas.
            FieldMd("UsuarioId", "ID do Usuário", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Status", "Status", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Acao", "Ação de Cobrança", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Situacao", "Situação", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Ocorrencia", "Ocorrência", "@COB_TITULO_L", DbType.db_Alpha),
            FieldMd("Observacao", "Observação", "@COB_TITULO_L", DbType.db_Memo),
            FieldMd("Cobrador", "Cobrador", "@COB_TITULO_L", DbType.db_Alpha),
            // A promessa nasce numa acao especifica. Sem guardar por linha, remover a acao que
            // prometeu deixava a data no cabecalho pra sempre e o titulo seguia contando como
            // "promessa vencida" no dashboard - nao havia de onde recompor.
            FieldMd("DataPromessa", "Data Prometida", "@COB_TITULO_L", DbType.db_Date),
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

        // Auditoria das remocoes do historico: a linha sai da UDT e o SAP nao versiona, entao o
        // conteudo apagado fica aqui. Guarda quem apagou, quando, e a linha inteira.
        listOf(
            FieldMd("Registro", "Registro de Cobrança", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("LineId", "Nº da Linha", "@COB_TITULO_LOG", DbType.db_Numeric),
            FieldMd("RemovidoPor", "Removido por", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("RemovidoPorId", "ID de quem removeu", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("RemovidoEm", "Removido em", "@COB_TITULO_LOG", DbType.db_Date),
            FieldMd("RemovidoHora", "Hora da Remoção", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Autor", "Autor da Ação", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("AutorId", "ID do Autor", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Data", "Data da Ação", "@COB_TITULO_LOG", DbType.db_Date),
            FieldMd("Hora", "Hora da Ação", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Status", "Status", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Acao", "Ação de Cobrança", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Situacao", "Situação", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Ocorrencia", "Ocorrência", "@COB_TITULO_LOG", DbType.db_Alpha),
            FieldMd("Observacao", "Observação", "@COB_TITULO_LOG", DbType.db_Memo),
            FieldMd("DataPromessa", "Data Prometida", "@COB_TITULO_LOG", DbType.db_Date),
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

        // UDO (e nao UDT solta) pelo mesmo motivo do COB_DOMINIO: e o caminho que o Service Layer
        // expoe como /b1s/v1/COB_TITULO_LOG, ja usado e testado neste projeto.
        val cobTituloLog = UserDefinedObject(
            "COB_TITULO_LOG", "Cobrança - Log de Remoção", "COB_TITULO_LOG", UDOObjType.boud_MasterData
        )
        udoService.findOrCreate(cobTituloLog)

        listOf(
            UserKeyMD(
                "ukCobTit2", "@COB_TITULO", YesNo.tYES,
                listOf(Elements("Tipo"), Elements("DocEntry"), Elements("InstlmntID"))
            ),
        ).forEach { userKeyService.findOrCreate(it) }
    }
}
