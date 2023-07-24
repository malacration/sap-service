package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.DbType
import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class PedidoFieldConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        val updatePedido = FieldMd("pedido_update","Atualização pedido","ORDR")
                .also { it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM")) }
        userFieldsMDService.findOrCreate(updatePedido)

        val idPedidoForca = FieldMd("id_pedido_forca","Id Pedido Venda","ORDR").also {
            it.size = 20
        }
        userFieldsMDService.findOrCreate(idPedidoForca)

        val precoNegociado = FieldMd("preco_negociado","Preço Negociado","DRF1", DbType.db_Float)
        userFieldsMDService.findOrCreate(precoNegociado)

        val precoBase = FieldMd("preco_base","Preço base","DRF1", DbType.db_Float)
        userFieldsMDService.findOrCreate(precoBase)

        val idForcaVendas = FieldMd("id_forca","Id Força de vendas","OCRD",DbType.db_Numeric)
                .also {
                    it.size = null
                }
        userFieldsMDService.findOrCreate(idForcaVendas)

        val idItemForca = FieldMd("id_item_forca","Id Item Força","DRF1").also {
            it.size = 100
        }
        userFieldsMDService.findOrCreate(idItemForca)


        val fluxoVendaPrazo = FieldMd("fazer_fluxo_prazo","Fazer Fluxo Prazo?","OCRD")
                .also {
                    it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM"))
                    it.defaultValue = "0"
                }
        userFieldsMDService.findOrCreate(fluxoVendaPrazo)


        listOf(
            FieldMd("pix_textContent","Text Content - PIX","INV6", DbType.db_Memo),
            FieldMd("pix_link","Link - PIX","INV6", DbType.db_Memo),
            FieldMd("pix_reference","Reference - PIX","INV6", DbType.db_Alpha),
        ).forEach { userFieldsMDService.findOrCreate(it) }

        userFieldsMDService.findOrCreate(
            FieldMd("pix_reference","Reference - PIX","ORCT",DbType.db_Alpha))

        listOf(
            FieldMd("gerar_pix","Gerar Pix?","OPYM")
                .also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM"))
                it.defaultValue = "0"
            },
        ).forEach { userFieldsMDService.findOrCreate(it)}

        listOf(
            FieldMd("publica_forca","Subir Força de venda?","OPLN")
            .also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM"))
                it.defaultValue = "0"
            },
        ).forEach { userFieldsMDService.findOrCreate(it) }


        listOf(
            FieldMd("idTabela","Id Tabela preco","INV1",DbType.db_Numeric),
        ).forEach { userFieldsMDService.findOrCreate(it) }



    }
}