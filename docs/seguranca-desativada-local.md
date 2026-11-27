# Consulta de contas a pagar sem autenticacao (somente teste local/homologacao)

> **ATENCAO: somente para teste local ou de homologacao. NUNCA em producao.**
> Nunca coloque esta chave em `application*.properties` versionado (default, hmg ou prd),
> em Dockerfile ou em Compose versionado. Use apenas variavel de ambiente na maquina de teste.

Para testar o fluxo Nota Agil -> sap-rovema sem a service account do Keycloak
(`integracao_contas_pagar`), existe a chave:

| Variavel de ambiente | Propriedade equivalente | Padrao |
| --- | --- | --- |
| `SAP_ROVEMA_SECURITY_DISABLED=true` | `sap-rovema.seguranca.desativada=true` | desligada |

A propriedade, quando definida, prevalece sobre a variavel (`sap-rovema.seguranca.desativada=false`
desliga mesmo com a variavel ligada). Pelo relaxed binding do Spring a propriedade tambem responde a
`SAP_ROVEMA_SEGURANCA_DESATIVADA`; ao procurar a chave na configuracao de um ambiente, procure por
`SAP_ROVEMA_SECURITY_DISABLED`, `SAP_ROVEMA_SEGURANCA_DESATIVADA` e `sap-rovema.seguranca.desativada`.

Os nomes sao especificos do sap-rovema de proposito: `seguranca.desativada` ou um
`SEGURANCA_DESATIVADA` generico (ex.: num arquivo de ambiente compartilhado com o Nota Agil)
**nao** ligam a chave.

## O que ela libera

Somente `POST /contas-pagar/pagamento-parcela/consulta` (caminho exato, sem o context-path),
e somente quando a requisicao **nao** traz `Authorization`:

- a requisicao passa como o usuario sintetico `auth-disabled-local`, com a unica role
  `integracao_contas_pagar` (sem filial, sem vinculo de vendedor);
- o `RoleBasedAuthorizationFilter` continua aplicando as regras: a role precisa autorizar a rota na
  fonte de regras ativa (`rules.yml` por padrao; com `acesso.regras.fonte=sap`, a regra precisa
  existir no UDO);
- qualquer outra rota, outro metodo (ex.: `GET`) ou caminho parecido continua exigindo autenticacao;
- requisicao com `Authorization` segue o fluxo normal (token invalido continua recusado).

Nao confundir com `spring.security.disable`, que abre a API inteira como um usuario admin.
Com `spring.security.disable=true` esta chave nao muda nada (a cadeia ja e `permitAll`).

## Protecoes

- **Nao sobe com profile de producao**: se a chave estiver ligada e algum profile for `prd`,
  `prod`, `production` ou `producao` (sem diferenciar maiusculas), ativo (`SPRING_PROFILES_ACTIVE`)
  ou padrao (`SPRING_PROFILES_DEFAULT` / `spring.profiles.default`), a aplicacao falha na subida com
  a mensagem `sap-rovema.seguranca.desativada / SAP_ROVEMA_SECURITY_DISABLED=true nao e permitido
  com o profile [...]`.
- **Limitacao**: sem profile de producao nenhum (ex.: producao rodando sem profile e com
  `application.properties` externo) a protecao nao tem como reconhecer producao. Confira o
  `SPRING_PROFILES_ACTIVE` do ambiente.
- Na subida, um `WARN` destacado informa que a chave esta ligada.
- Cada requisicao aceita sem autenticacao gera `WARN` com metodo e caminho
  (`SEGURANCA DESATIVADA: POST /contas-pagar/pagamento-parcela/consulta aceita sem autenticacao ...`),
  sem corpo nem dados da consulta.

## Uso no teste ponta a ponta

```shell
SAP_ROVEMA_SECURITY_DISABLED=true SPRING_PROFILES_ACTIVE=hmg ./gradlew bootRun
```

No Nota Agil local, ligar `NOTA_AGIL_SECURITY_DISABLED=true` e configurar so
`INTEGRACAO_ERP_SAP_URL` (sem `TOKENURL`/`CLIENTID`/`CLIENTSECRET`): ele chama este endpoint sem
token. Ver `nota.agil/back/docs/keycloak.md`.

Implementacao: `SegurancaDesativadaContasPagar` e `SegurancaDesativadaContasPagarFilter`
(`infrastructure/security`); testes em `SegurancaDesativadaContasPagarTest`.
