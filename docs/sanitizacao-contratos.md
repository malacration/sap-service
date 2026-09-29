# Sanitização de contratos de venda futura

Acesso administrativo em **Configurações → Sanitização de contratos** no front-sap.
`GET /sanitizacao-contratos/previa` consulta o HANA pelo sap-odbc, sem gravar.
`POST /sanitizacao-contratos/aplicar` recebe `{ "previaId": "...", "transId": 123 }`.

A consulta percorre todas as reclassificações VFEC ainda não estornadas, com nota de entrega
cancelada ou com devolução ativa. O vínculo usa Ref1/DocNum, parceiro e filial. A reconciliação
é verificada por **ITR1.TransId + TransRowId**, considerando somente reconciliações ativas.
As apropriações são localizadas por U_TX_DocEntryRef e pela sequência configurada.

A prévia mostra as duas pernas, saldos, notas de origem, devoluções, apropriações (incluindo
as já canceladas), ações e todos os participantes das reconciliações afetadas. O usuário
seleciona e confirma os itens. Devoluções parciais são apenas sinalizadas. A devolução integral
exige comprovação das quantidades devolvidas de cada linha; devoluções avulsas identificadas
por referência ou reconciliação ficam para conferência quando não há essa comprovação.

Também ficam para conferência: as duas pernas reconciliadas, vínculos ambíguos, VFDV já existente,
devolução ainda pendente do processamento automático, moeda estrangeira e divergências de
estrutura, contas, valor, parceiro ou filial. Uma consulta truncada falha sem produzir prévia parcial.

Para cada item elegível, um único changeset do Service Layer cancela as reconciliações,
cancela as apropriações ativas e cancela/estorna o lançamento contábil. São usadas as ações
nativas `InternalReconciliationsService_Cancel`, `Invoices(id)/Cancel` e `JournalEntries(id)/Cancel`.
Não há atualização direta de tabelas do SAP. As ações Cancel não aceitam data e as datas originais
não são alteradas. Em HMG (contrato 108, set/2026) o estorno e o documento de cancelamento saíram
com a data do documento original, não com a data corrente. Como isso pode depender da configuração
da empresa, a conferência aceita a data do original ou a data do SAP lida imediatamente antes do
changeset; qualquer outra data resulta em CONFERIR. Após o changeset são conferidos os documentos
de cancelamento, suas datas, as apropriações, as reconciliações (inclusive as de adiantamento,
tipo 16, que o SAP desfaz ao cancelar a apropriação) e o saldo da reclassificação.

Somente as reconciliações manuais (ReconType 0) são canceladas explicitamente. A reconciliação de
adiantamento (ReconType 16, IsSystem = Y) é do sistema: o SAP recusa cancelá-la e a desfaz sozinho.

Referência das ações: [SAP Service Layer API Reference](https://help.sap.com/doc/056f69366b5345a386bb8149f1700c19/10.0/en-US/Service%20Layer%20API%20Reference.html).

A prévia fica em memória por 30 minutos, vinculada ao usuário que a consultou, e é comparada
com uma nova leitura completa do item antes da escrita. Reiniciar o backend invalida as prévias;
em instalações com múltiplas réplicas, usar afinidade de sessão para esse fluxo. Repetir a mesma
confirmação na mesma prévia devolve o resultado anterior. Uma falha ou resposta incerta interrompe
o lote na tela e exige conferência e nova prévia. O SAP mantém a atomicidade por reclassificação;
itens anteriormente concluídos permanecem concluídos. O log registra usuário, lançamento,
reconciliações, apropriações e resultado.

Configuração necessária: `odbc.base-url`, `odbc.api-key`, `venda-futura.sequencia_adiantamento`
e `venda-futura.conta-controle`, apontando para a mesma empresa SAP do Service Layer.

Validação local:

```sh
./gradlew test --tests 'br.andrew.sap.services.comercial.sanitizacao.*' --offline -x import-ws
python3 -m unittest discover -s src/test/python -p test_sanitizacao_contratos_sql.py
```

Os testes Python executam fixtures relacionais em SQLite em memória, com o SQL exatamente como
é enviado (sem tradução).
Eles não substituem a execução das consultas no HANA. Antes da primeira efetivação em produção,
homologar na versão instalada do SAP as ações Cancel em changeset, a data dos documentos gerados
e a liberação dos adiantamentos após cancelamento da apropriação. Em HMG os dois itens do
contrato 108 foram efetivados e conferidos (set/2026). Se os períodos dos documentos originais
estiverem fechados em produção, o SAP recusa o changeset e o item volta como REJEITADO.
