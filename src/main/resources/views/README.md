# Views SQL

Cada `.sql` desta pasta (recursivamente) é enviado no boot da aplicação para o
SAP B1 como uma entidade `SQLQueries` do Service Layer, por
`ProvisioningConfiguration.createQuerys()`.

## Regra: sem comentários

**Não escreva comentários dentro destes arquivos.** Nem `--`, nem `/* */`.

`Query.kt` achata o SQL em uma única linha (`\n`, `\r` e `\t` viram espaço)
antes de enviar. Com isso, um `--` não comenta apenas a sua linha: comenta
**todo o resto da query**, e a view sobe truncada para o SAP — falhando ou
retornando o conjunto errado, silenciosamente.

`/* */` sobrevive ao achatamento, mas também não é aceito: acentos e caracteres
especiais no comentário podem corromper o payload JSON do Service Layer, e o
texto fica gravado no `SqlText` da view no banco do cliente.

Documente a lógica no Kotlin que chama a view ou num teste em
`src/test/kotlin/**` (ver `CobrancaTitulosSqlTest`,
`ContratoVendaFuturaStatusSqlTest`). Nomes de arquivo descritivos > comentários.

## Parâmetros

Use `:nomeDoParametro` no SQL e passe `Parameter("nomeDoParametro", valor)` no
`SqlQueriesService.execute`. O nome do arquivo (com extensão) é o `sqlCode` /
`sqlName` da view no SAP — renomear o arquivo cria uma view nova.
