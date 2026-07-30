package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.sap.sistema.TableMd
import br.andrew.sap.model.sap.sistema.TbType
import br.andrew.sap.services.structs.UserFieldsMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Cria os campos e a tabela novos usados pelo frete por regiao: filial, distancia
 * e faixas de preco progressivo. O UDO em si (Code "Regiao", sem acento - o Code
 * antigo "Região" tinha um bug no Service Layer que derrubava qualquer acesso por
 * chave) e as tabelas @RO_REGIAO/@RO_REGIAO_LINHAS ja existem, criados manualmente
 * no SAP. Aqui so criamos o que ainda nao existe - tudo passa por findOrCreate,
 * nada e recriado/sobrescrito.
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.regiao-frete"], havingValue = "true", matchIfMissing = false)
class RegiaoFreteConfiguration(
    val userFieldsMDService: UserFieldsMDService,
    val tableService: UserTablesMDService
) {

    init {
        userFieldsMDService.findOrCreate(
            FieldMd("Filial", "Filial", "@RO_REGIAO", DbType.db_Numeric)
        )
        userFieldsMDService.findOrCreate(
            FieldMd("Distancia", "Distância (km)", "@RO_REGIAO_LINHAS", DbType.db_Float)
        )

        tableService.findOrCreate(
            TableMd("RO_REGIAO_FAIXA", "Regiao Faixa de Preco", TbType.bott_MasterDataLines)
        )
        listOf(
            FieldMd("QtdeAte", "Quantidade Até", "@RO_REGIAO_FAIXA", DbType.db_Numeric),
            FieldMd("ValorKm", "Valor por Km", "@RO_REGIAO_FAIXA", DbType.db_Float),
        ).forEach { userFieldsMDService.findOrCreate(it) }
    }
}
