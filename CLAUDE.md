# Instruções do projeto — sap-service

Middleware Kotlin/Spring Boot para o SAP B1 Service Layer.

## Views SQL (`src/main/resources/views/**/*.sql`)

**NUNCA escreva comentários dentro de um arquivo `.sql` de view.** Nem `--`, nem
`/* */`. Um comentário quebra a view em produção.

### Por quê

Esses arquivos não são executados localmente: no boot,
`ProvisioningConfiguration.createQuerys()`
(`src/main/kotlin/br/andrew/sap/infrastructure/create/views/ProvisiongConfig.kt`)
lê cada `.sql` do classpath e envia o conteúdo para o SAP B1 como uma entidade
`SQLQueries` do Service Layer.

Antes de subir, `Query` (`src/main/kotlin/br/andrew/sap/model/sistema/Query.kt`)
**achata o SQL inteiro em uma única linha**:

```kotlin
val sqlText = sqlText.replace("\t"," ").replace("\n", " ").replace("\r", " ")
```

Com todas as quebras de linha viradas espaço, um `--` deixa de comentar só a
linha dele e passa a **comentar todo o resto da query**. O que sobe para o SAP é
um SQL truncado. Exemplo:

```sql
SELECT r."ReconNum"   -- só as ativas
FROM "OITR" r
WHERE r."Canceled" = 'N'
```

vira, no SAP:

```sql
SELECT r."ReconNum"   -- só as ativas FROM "OITR" r WHERE r."Canceled" = 'N'
```

→ `SELECT r."ReconNum"` sem `FROM`, sem `WHERE`. A view falha ou, pior, retorna
o conjunto errado.

`/* */` sobrevive ao achatamento, mas também é proibido: acentos e caracteres
especiais dentro do comentário podem corromper o payload JSON enviado ao Service
Layer, e o comentário fica gravado no `SqlText` da view no banco do cliente.

### O que fazer no lugar

Precisa explicar a lógica de uma view? Escreva o comentário **fora** do `.sql`:

- no Kotlin que chama a view (o método em `...Service.kt` que passa o nome do
  arquivo para `SqlQueriesService.execute`);
- ou num teste em `src/test/kotlin/**` — já existe esse padrão
  (`CobrancaTitulosSqlTest`, `ContratoVendaFuturaStatusSqlTest`, etc.: eles leem
  o `.sql` com `Files.readString` e documentam/validam as regras dele).

O nome do arquivo é o contrato com o SAP (`sqlCode` = `sqlName` = nome do
arquivo), então use nomes descritivos em vez de comentários explicativos.
