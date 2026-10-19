package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.services.security.acesso.RegrasAcessoService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Cria a versao 1 das regras de acesso a partir do arquivo em uso no servidor (em producao, o
 * `/app/rules.yml` do swarm), so se o UDO ainda estiver vazio. Recebe AcessoRegrasConfiguration no
 * construtor apenas para garantir que a tabela/UDO ja existe antes de semear (mesmo truque do
 * TravaRegraSeeder).
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.acesso"], havingValue = "true", matchIfMissing = false)
class AcessoRegrasSeeder(
    acessoRegrasConfiguration: AcessoRegrasConfiguration,
    val service: RegrasAcessoService
) {

    init {
        service.semearSeVazio()
    }
}
