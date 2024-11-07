package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class DocumentConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        FieldMd("assinatura","Envia Assinatura","ORDR")
            .also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                it.defaultValue = "0"
                userFieldsMDService.findOrCreate(it)
            }

        listOf(
            FieldMd("uuid_forca","UUID Força de Vendas","OINV"),
            FieldMd("entrega_vf","Entrega Venda Futura","OINV")
                .also {
                    it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                    it.defaultValue = "0"
                },
            FieldMd("pedido_update","Atualiza pedido?","OINV").also {
                it.ValidValuesMD = listOf(ValuesMd("0","NÃO"), ValuesMd("1","SIM"))
                it.defaultValue = "1"
            },

            FieldMd("rd_station","rdStation","OINV").also {
                it.size = 30
            }
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }
}