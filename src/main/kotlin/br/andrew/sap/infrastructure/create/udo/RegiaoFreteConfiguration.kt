package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.FormColumns
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
 * Cria a estrutura completa do UDO "Regiao" na tabela AR_REGIAO. A tabela
 * anterior, RO_REGIAO, foi criada manualmente no SAP sem os defaults certos
 * (CanCreateDefaultForm/CanLog) e ficou com o Enhanced Form nunca compilado -
 * ver o diagnostico do clone "Regiao2" que confirmou o problema. AR_REGIAO
 * substitui RO_REGIAO usando o mesmo mecanismo de findOrCreate que criou o
 * "Regiao2" certo.
 *
 * Estrutura: @AR_REGIAO (NomeRegiao, CodCordenador, Filial, Ativa), @AR_REGIAO_LINHAS
 * (Locais, Distancia), @AR_REGIAO_FAIXA (QtdeAte, ValorKm).
 *
 * O Code do UDO continua "Regiao" (RegiaoService.path() nao muda). Se um UDO
 * "Regiao" ja existir no ambiente apontando pra RO_REGIAO, ele precisa ser
 * apagado manualmente no SAP (Ferramentas > Personalizacao > Objetos
 * Definidos pelo Usuario) antes de ligar essa flag - o Service Layer nao
 * permite reapontar o TableName de um UDO ja registrado.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.regiao-frete"], havingValue = "true", matchIfMissing = false)
class RegiaoFreteConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd("AR_REGIAO", "Regiao", TbType.bott_MasterData),
            TableMd("AR_REGIAO_LINHAS", "Regiao Linhas", TbType.bott_MasterDataLines),
            TableMd("AR_REGIAO_FAIXA", "Regiao Faixa de Preco", TbType.bott_MasterDataLines),
        ).forEach { tableService.findOrCreate(it) }

        listOf(
            FieldMd("NomeRegiao", "Nome da Região", "@AR_REGIAO", DbType.db_Alpha),
            FieldMd("CodCordenador", "Codigo Cordenador", "@AR_REGIAO", DbType.db_Alpha),
            FieldMd("Filial", "Filial", "@AR_REGIAO", DbType.db_Numeric),
            //custo por unidade pra levar o produto da fabrica ate a unidade dessa
            //regiao - somado ao valor por unidade da faixa (ver Regiao.calcularFrete)
            FieldMd("CustoTransporte", "Custo de Transporte por Unidade", "@AR_REGIAO", DbType.db_Float),
            //varias regioes podem compartilhar a mesma filial, mas so uma pode
            //estar ativa por filial ao mesmo tempo - toda regiao nova comeca
            //desativada (ver RegiaoService.ativar/criar)
            FieldMd("Ativa", "Região Ativa?", "@AR_REGIAO").also {
                it.ValidValuesMD = listOf(ValuesMd("0", "NÃO"), ValuesMd("1", "SIM"))
                it.defaultValue = "0"
            },
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("Locais", "Locais", "@AR_REGIAO_LINHAS", DbType.db_Alpha),
            FieldMd("Distancia", "Distância (km)", "@AR_REGIAO_LINHAS", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        listOf(
            FieldMd("QtdeAte", "Quantidade Mínima", "@AR_REGIAO_FAIXA", DbType.db_Numeric),
            //nome do campo (ValorKm) preservado por compatibilidade com o UDO ja
            //criado, mas o valor cadastrado e por bloco de 100km, nao por km -
            //evita ter que cadastrar fracoes de centavo pra cada km rodado
            FieldMd("ValorKm", "Valor a cada 100Km", "@AR_REGIAO_FAIXA", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        udoService.findOrCreate(getUserDefined())
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject(
            "Regiao", "Região",
            "AR_REGIAO",
            UDOObjType.boud_MasterData
        )
        ud.popChildTable(ChildTables("AR_REGIAO_LINHAS"))
        ud.popChildTable(ChildTables("AR_REGIAO_FAIXA"))
        //so colunas de cabecalho - colunas de tabela filha aqui deram erro de
        //validacao no SAP (nao sao necessarias, so afetam o layout do form
        //nativo, que nao usamos - a tela e via API/Angular)
        ud.UserObjectMD_FormColumns.addAll(listOf(
            FormColumns("Code", "Código", 0),
            FormColumns("Name", "Nome", 0),
        ))
        return ud
    }
}
