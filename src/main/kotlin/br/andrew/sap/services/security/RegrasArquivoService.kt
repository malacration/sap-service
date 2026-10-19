package br.andrew.sap.services.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import br.andrew.sap.services.security.interfaces.RuleService as iRuleService

/**
 * Regras de acesso lidas de um arquivo YAML (o `rules.yml`). E a fonte de sempre e continua
 * sendo a padrao (`acesso.regras.fonte=arquivo`).
 *
 * O arquivo e lido e interpretado uma unica vez, na primeira consulta. Antes ele era relido a
 * cada `get`, umas 5 vezes por requisicao. Um arquivo montado pelo swarm nao muda dentro do
 * mesmo container (a config e recriada com nome novo a cada alteracao), entao nao ha o que
 * reler.
 *
 * O local vem de `acesso.regras.arquivo`. O padrao `classpath:rules.yml` reproduz o
 * comportamento anterior: em producao o `/app/rules.yml` do swarm so vence o arquivo do jar
 * porque o JarLauncher roda a partir de /app sem `-cp`. Apontar `file:/app/rules.yml` tira a
 * dependencia desse efeito colateral.
 */
@Service
class RegrasArquivoService @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    @Value("\${acesso.regras.arquivo:classpath:rules.yml}") private val local: String,
) : iRuleService {

    constructor(resourceLoader: ResourceLoader) : this(resourceLoader, "classpath:rules.yml")

    private val yamlMapper = YAMLMapper().registerKotlinModule()

    private class Carregado(val texto: String, val regras: Map<String, List<Rule>>)

    private val carregado: Carregado by lazy {
        val texto = resourceLoader.getResource(local).inputStream.use { String(it.readAllBytes(), Charsets.UTF_8) }
        Carregado(texto, yamlMapper.readValue(texto, RoleRules::class.java).roles)
    }

    override fun get(role: String): List<Rule> = carregado.regras[role] ?: emptyList()

    /** Todos os perfis do arquivo, na ordem em que aparecem nele. */
    fun todas(): Map<String, List<Rule>> = carregado.regras

    /** O texto do arquivo exatamente como esta no servidor. */
    fun textoBruto(): String = carregado.texto

    fun local(): String = local
}

data class RoleRules @JsonCreator constructor(
    @JsonProperty("roles") val roles: Map<String, List<Rule>>
)
