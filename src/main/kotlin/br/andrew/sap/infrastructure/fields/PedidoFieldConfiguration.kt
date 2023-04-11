package br.andrew.sap.infrastructure.fields

import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.ValuesMd
import br.andrew.sap.services.UserFieldsMDService
import org.springframework.context.annotation.Configuration


@Configuration
class PedidoFieldConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        val updatePedido = FieldMd("pedido_update","Atualização pedido","ORDR")
                .also { it.ValidValuesMD = listOf(ValuesMd("0","NÃO"),ValuesMd("1","SIM")) }
        userFieldsMDService.findOrCreate(updatePedido)
    }
}