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
@ConditionalOnProperty(value = ["rv.campos"], havingValue = "true", matchIfMissing = false)
class OrdemCarregamentoConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val udoService: UserObjectsMDService,
    val userKeyService: UserKeyMDService,
    val tableService: UserTablesMDService
) {

    init {
        listOf(
            TableMd(
                "RV_ORD_CARREGAMENTO", "Ordem de Carregamento", TbType.bott_Document
            ),
            TableMd(
                "RV_ORD_LINHA", "Ordem de Carregamento Linha", TbType.bott_DocumentLines
            ),
        ).forEach { tableService.findOrCreate(it) }

        listOf(
            //TODO verificar se esse campo e usando realmente, pq uma ordem deveria ter varios pedidos, nesse caso tem somente uma
            FieldMd("orderDocEntry", "Pedido", "@RV_ORD_CARREGAMENTO", null).also {
                it.LinkedSystemObject = LinkedSystemObject.ulOrders
                it.subType = "st_None"
            },
            //TODO nao precisa disso
            FieldMd("cardCode", "Cliente", "@RV_ORD_CARREGAMENTO", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulBusinessPartners
            },
            //TODO nao precisa disso
            FieldMd("cardName", "Nome Cliente", "@RV_ORD_CARREGAMENTO", DbType.db_Alpha),

            //TODO nao precisa disso
            FieldMd("dataCriacao", "Data de Criação", "@RV_ORD_CARREGAMENTO", DbType.db_Date),

            //TODO nao precisa disso
            FieldMd("valorFrete", "Valor Frete", "@RV_ORD_CARREGAMENTO", DbType.db_Float),

            //TODO verificar se o carregamento pode conter mutiplas filiais
            FieldMd("filial", "Filial", "@RV_ORD_CARREGAMENTO", DbType.db_Numeric),

            //TODO Nao precisa
            FieldMd("vendedor", "Vendedor", "@RV_ORD_CARREGAMENTO", DbType.db_Float).also {
                it.LinkedSystemObject = LinkedSystemObject.ulInventoryGenExit
            },
            //TODO Nao precisa, acredito que ja tem docEntry ou code
            FieldMd("orderCode", "Código Ordem", "@RV_ORD_CARREGAMENTO", DbType.db_Alpha),
            //todo o sap cria por padrao description, mas vale confirmar. se nao for o caso deveria usar somente name e nao orderName
            FieldMd("ordemName", "Nome Ordem", "@RV_ORD_CARREGAMENTO", DbType.db_Alpha),
            //TODO nao precisa do total do frete
            FieldMd("totalFrete", "Total Frete", "@RV_ORD_CARREGAMENTO", DbType.db_Float),
            //TODO nao precisa do total do frete
            FieldMd("pesoTotal", "Peso Total", "@RV_ORD_CARREGAMENTO", DbType.db_Float),
            //Provavelmente sera necessario fazer uma view que retorne todos os dados da colecao de pedidos do carregamento, tais como frete, peso entre outros
            FieldMd("valorTotal", "Valor Total", "@RV_ORD_CARREGAMENTO", DbType.db_Float),
            //TODO a ordem de carregamento pode ter diferentes regioes?
            FieldMd("codRegiao", "Código Região", "@RV_ORD_CARREGAMENTO", DbType.db_Numeric),
            //TODO nao precisa
            FieldMd("nomeRegiao", "Nome Região", "@RV_ORD_CARREGAMENTO", DbType.db_Alpha),
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        //TODO fazer uma transaction notification para a ordem de carreggamento,
        // quando um pedido estiver em uma ordem de carregamento, somente o usuario de integracao pode mudar o status do pedido?
        // Ou se o pedido indo para closed que no caso esta sendo faturado, ai sim pode modificar!
        // Line fields
        listOf(
            FieldMd("docEntryPedido", "DocEntry Pedido", "@RV_ORD_LINHA", DbType.db_Numeric).also {
                it.LinkedSystemObject = LinkedSystemObject.ulOrders
            },
            //TODO do que se trata o docnum?
            FieldMd("docNum", "DocNum Pedido", "@RV_ORD_LINHA", DbType.db_Numeric),
            FieldMd("codItemPedido", "Código Item Pedido", "@RV_ORD_LINHA", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulItems
            },
            FieldMd("nameItemPedido", "Nome Item Pedido", "@RV_ORD_LINHA", DbType.db_Alpha),
            FieldMd("codCliente", "Código Cliente", "@RV_ORD_LINHA", DbType.db_Alpha).also {
                it.LinkedSystemObject = LinkedSystemObject.ulBusinessPartners
            },
            FieldMd("nameCliente", "Nome Cliente", "@RV_ORD_LINHA", DbType.db_Alpha),
            FieldMd("quantidadePedido", "Quantidade Pedido", "@RV_ORD_LINHA", DbType.db_Float),
            FieldMd("unidadeMedidaPedido", "Unidade Medida Pedido", "@RV_ORD_LINHA", DbType.db_Alpha),
            FieldMd("fretePedido", "Frete Pedido", "@RV_ORD_LINHA", DbType.db_Float),
            FieldMd("precoNegociadoPedido", "Preço Negociado Pedido", "@RV_ORD_LINHA", DbType.db_Float),
            FieldMd("codRegiao", "Código Região", "@RV_ORD_LINHA", DbType.db_Numeric),
            FieldMd("nomeRegiao", "Nome Região", "@RV_ORD_LINHA", DbType.db_Alpha),
            FieldMd("pesoPedido", "Peso Pedido", "@RV_ORD_LINHA", DbType.db_Float),
            // Keeping existing fields for consistency
            FieldMd("precoBase", "Preço Base", "@RV_ORD_LINHA", DbType.db_Float),
            FieldMd("desconto", "Desconto Vendedor", "@RV_ORD_LINHA", DbType.db_Float),
            FieldMd("comissao", "Comissão", "@RV_ORD_LINHA", DbType.db_Float),
        ).forEach {
            userFieldsMDService.findOrCreate(it)
        }

        val udoProperties = getUserDefined()
        udoService.findOrCreate(udoProperties)

        listOf(
            UserKeyMD("ukOrder", "@RV_ORD_CARREGAMENTO", YesNo.tYES, listOf(Elements("orderDocEntry"))),
        ).forEach { userKeyService.findOrCreate(it) }

        listOf(
            FieldMd("venda_futura", "Contrato Venda Futura", "OINV", DbType.db_Alpha)
                .also { it.linkedUDO = udoProperties.Code },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }

    private fun getUserDefined(): UserDefinedObject {
        val ud = UserDefinedObject(
            "RV_ORD_CARREGAMENTO",
            "Ordem de Carregamento",
            "RV_ORD_CARREGAMENTO",
            UDOObjType.boud_Document,
            YesNo.tNO
        )
        ud.popChildTable(ChildTables("RV_ORD_LINHA"))
        ud.UserObjectMD_FormColumns.addAll(
            listOf(
                FormColumns("DocEntry", "DocEntry", 0),
                FormColumns("DocNum", "DocNum", 0)
            )
        )
        ObjectMapper().registerModule(KotlinModule()).writeValueAsString(ud)
        return ud
    }
}