package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.ValuesMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.sustennutri"], havingValue = "true", matchIfMissing = false)
class SustenutriFieldsConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        listOf(
            FieldMd("grupo_sustennutri","Grupo","OITM")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("sal","SAL MINERAL"),
                    ValuesMd("racao","RAÇÃO"),
                    ValuesMd("quirela","QUIRERA"),
                    ValuesMd("nucleo","NÚCLEOS E CONCENTRADOS"),
                    ValuesMd("milho","MILHO"),
                    ValuesMd("fora","FORA DE LINHA"),
                    ValuesMd("farelo","FARELADA"),
                    ValuesMd("ener-prot","ENERGÉTICO - PROTEICO"),
                    ValuesMd("adensado","ADENSADO"),
                ) },
            FieldMd("linha_sustennutri","linha","OITM")
                .also { it.ValidValuesMD = listOf(
                    ValuesMd("especial","ESPECIAL"),
                    ValuesMd("fora","FORA DE LINHA"),
                    ValuesMd("mega","OX MEGA"),
                    ValuesMd("power","OX POWER"),
                    ValuesMd("premium","OX PREMIUM"),
                    ValuesMd("farelado","FARELADA"),
                    ValuesMd("terceiro","TERCEIROS"),
                ) },
        FieldMd("categoria","categoria","OITM")
            .also { it.ValidValuesMD = listOf(
                ValuesMd("bov","BOVINOS"),
                ValuesMd("equino","EQUINOS"),
                ValuesMd("suino","SUINOS"),
                ValuesMd("aves","AVES"),
            ) },
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }
}