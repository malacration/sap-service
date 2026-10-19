package br.andrew.sap.services.security.acesso

import br.andrew.sap.infrastructure.create.udo.AcessoRegrasSeeder
import br.andrew.sap.services.security.interfaces.RuleService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Liga as regras do SAP como fonte de autorizacao (`acesso.regras.fonte=sap`). Com o valor padrao
 * (`arquivo`) nada disto existe e o RegrasArquivoService continua sendo a unica implementacao.
 *
 * O filtro e o KeycloakUserMapper injetam a INTERFACE RuleService; o `@Primary` abaixo e o que faz
 * eles receberem a versao do SAP quando ela esta ligada.
 */
@Configuration
@ConditionalOnProperty(name = ["acesso.regras.fonte"], havingValue = "sap")
class RegrasAcessoFonteConfiguration {

    @Bean
    fun regrasAcessoCache(
        repositorio: ObjectProvider<RegrasAcessoRepositorio>,
        seeder: ObjectProvider<AcessoRegrasSeeder>,
    ): RegrasAcessoCache {
        val repo = repositorio.getIfAvailable() ?: throw IllegalStateException(
            "acesso.regras.fonte=sap exige fields.acesso=true (e um profile diferente de test): " +
                "sem isso o UDO ACESSO_REGRAS nao existe."
        )
        // So para o UDO ser criado e semeado ANTES da primeira leitura.
        seeder.getIfAvailable()
        return RegrasAcessoCache(repo).also { it.carregarNoBoot() }
    }

    @Bean
    @Primary
    fun sapRuleService(cache: RegrasAcessoCache): RuleService = SapRuleService(cache)
}
