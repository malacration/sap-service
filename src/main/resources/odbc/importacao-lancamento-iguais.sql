-- Pernas de debito dos lancamentos que tem a mesma referencia e a mesma data de alguma linha do
-- arquivo. O servico compara conta e valor: igual nos quatro e o mesmo lancamento ja importado
-- (pelo curl no /journal/save, por exemplo, que nao deixa registro em @LC_IMPORTACAO).
--
-- So a referencia nao basta: a mesma ordem de producao aparece como Ref1 nos lancamentos da propria
-- ordem, com outras contas. Por isso o casamento inclui conta de debito e valor.
--
-- Fora os estornos e os lancamentos estornados: reimportar o que foi estornado e legitimo.
--
-- Os filtros sao conjuntos independentes (referencias x datas x contas), entao a consulta tambem
-- traz combinacoes que nao estao no arquivo. A conta de debito corta quase todas, e o servico manda
-- as referencias em blocos para o resultado nao passar do limite do sap-odbc.
SELECT j."TransId", j."Number" AS "Numero", j."Ref1" AS "Referencia", j."RefDate" AS "Data", l."Account" AS "Conta", l."Debit" AS "Debito"
FROM "OJDT" j
INNER JOIN "JDT1" l ON l."TransId" = j."TransId"
WHERE j."Ref1" IN (:referencias)
  AND j."RefDate" IN (:datas)
  AND l."Debit" > 0
  AND l."Account" IN (:contas)
  AND j."StornoToTr" IS NULL
  AND NOT EXISTS (SELECT 1 FROM "OJDT" e WHERE e."StornoToTr" = j."TransId")
