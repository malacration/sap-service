package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.FormColumns
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
 * Clone estrutural completo do "Regiao" (Code "Regiao2"), criado do zero por
 * essa rotina (findOrCreate) com os defaults certos (CanCreateDefaultForm,
 * CanLog) - diferente do "Regiao" original, que foi criado via POST cru sem
 * passar por esses defaults e ficou com o Enhanced Form nunca compilado.
 *
 * Mesma estrutura campo a campo: @RO_REGIAO2 (NomeRegiao, CodCordenador,
 * Filial), @RO_REGIAO2_LINHAS (Locais, Distancia), @RO_REGIAO2_FAIXA (QtdeAte,
 * ValorKm). RegiaoService.path() e os @JsonProperty das colecoes em Regiao.kt
 * apontam pra ca temporariamente - ver comentarios "TESTE REGIAO2" nesses
 * arquivos - pra reaproveitar 100% da tela em Angular ja existente.
 *
 * Nada de producao depende disso. Reverter os 3 pontos marcados + apagar esse
 * UDO no SAP quando terminar o diagnostico.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.regiao2-teste"], havingValue = "true", matchIfMissing = false)
class Regiao2TesteConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd("RO_REGIAO2", "Teste Regiao2", TbType.bott_MasterData),
            TableMd("RO_REGIAO2_LINHAS", "Teste Regiao2 Linhas", TbType.bott_MasterDataLines),
            TableMd("RO_REGIAO2_FAIXA", "Teste Regiao2 Faixa", TbType.bott_MasterDataLines),
        ).forEach { tableService.findOrCreate(it) }

        listOf(
            FieldMd("NomeRegiao", "Nome da Região", "@RO_REGIAO2", DbType.db_Alpha),
            FieldMd("CodCordenador", "Codigo Cordenador", "@RO_REGIAO2", DbType.db_Alpha),
            FieldMd("Filial", "Filial", "@RO_REGIAO2", DbType.db_Numeric),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("Locais", "Locais", "@RO_REGIAO2_LINHAS", DbType.db_Alpha),
            FieldMd("Distancia", "Distância (km)", "@RO_REGIAO2_LINHAS", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("QtdeAte", "Quantidade Mínima", "@RO_REGIAO2_FAIXA", DbType.db_Numeric),
            FieldMd("ValorKm", "Valor por Km", "@RO_REGIAO2_FAIXA", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(getUserDefined())
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject(
            "Regiao2", "Regiao2 Teste",
            "RO_REGIAO2",
            UDOObjType.boud_MasterData
        )
        ud.popChildTable(ChildTables("RO_REGIAO2_LINHAS"))
        ud.popChildTable(ChildTables("RO_REGIAO2_FAIXA"))
        //so colunas de cabecalho - colunas de tabela filha aqui deram erro de
        //validacao no SAP (nao sao necessarias, so afetam o layout do form
        //nativo, que nao usamos - o teste e via API/Angular)
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code", "Código", 0),
            FormColumns("Name", "Nome", 0),
        ))
        return ud
    }
}
