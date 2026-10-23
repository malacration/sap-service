-- Status de documento da TaxPlus, para o filtro "Status SEFAZ" da tela de lote. Vem da
-- tabela e nao de uma lista fixa: os IDs sao da TaxPlus (4 = autorizada e o unico que o
-- resto do workspace usa por numero) e podem mudar com a versao do add-on.
SELECT "ID" AS "Id", "Description" AS "Descricao"
FROM "ProcessStatus"
ORDER BY "Description"
