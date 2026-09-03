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

## Regra: mudou parâmetro, crie uma `-v2`

A view **não vive na aplicação, vive no SAP**, e o `sqlCode` dela é o nome do
arquivo. Ou seja: ela é **global**, compartilhada por todas as APIs que apontam
para a mesma company. `QuerysServices.replace` sobrescreve a view existente no
boot — quem subir por último manda.

Por isso **acrescentar ou remover um `:parâmetro` é breaking change** para
qualquer versão da API que ainda esteja no ar chamando aquele nome. Foi
exatamente assim que a busca de produto da versão anterior parou: a v7.1
acrescentou `:superVendedor` em `order/produto-tabela.sql`, o boot reescreveu a
view no SAP, e a API antiga passou a chamar uma consulta que pede um parâmetro
que ela não manda.

Enquanto duas versões da API convivem:

- **não edite** os parâmetros do arquivo existente — ele é o contrato da versão
  antiga e fica congelado;
- crie `<nome>-v2.sql` com a consulta nova e aponte só a API nova para ele
  (ver `VIEW_PRODUTO_TABELA` em `ItemsService`);
- trave o contrato num teste (ver `BypassVendedorAdminTest`: a v1 não pode
  ganhar `:superVendedor` e a v2 só pode acrescentar esse parâmetro).

Mudar apenas o **corpo** da consulta, mantendo os mesmos parâmetros, continua
sendo compatível e não precisa de v2.
