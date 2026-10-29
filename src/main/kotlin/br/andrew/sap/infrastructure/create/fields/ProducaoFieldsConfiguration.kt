package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class ProducaoFieldsConfiguration(
    val userFieldsMDService: UserFieldsMDService
) {

    init {
        // Lote do produto acabado (ex.: AVC007565), impresso na ordem de producao e na de embalagem.
        // Campo proprio para nao depender do texto livre de Observacoes (Comments) da ordem.
        FieldMd("lote_producao", "Lote de Produção", "OWOR").also {
            it.size = 20
            userFieldsMDService.findOrCreate(it)
        }
    }
}
