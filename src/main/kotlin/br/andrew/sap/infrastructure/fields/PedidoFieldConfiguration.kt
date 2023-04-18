package br.andrew.sap.infrastructure.fields

import br.andrew.sap.model.DbType
import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.ValuesMd
import br.andrew.sap.services.UserFieldsMDService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
class PedidoFieldConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        val updatePedido = FieldMd("pedido_update","Atualização pedido","ORDR")
                .also { it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM")) }
        userFieldsMDService.findOrCreate(updatePedido)

        val idPedidoForca = FieldMd("id_pedido_forca","Id Pedido Venda","ORDR")
        userFieldsMDService.findOrCreate(idPedidoForca)

        val precoNegociado = FieldMd("preco_negociado","Preço Negociado","DRF1", DbType.db_Float)
        userFieldsMDService.findOrCreate(precoNegociado)

        val precoBase = FieldMd("preco_base","Preço base","DRF1", DbType.db_Float)
        userFieldsMDService.findOrCreate(precoBase)
    }
}