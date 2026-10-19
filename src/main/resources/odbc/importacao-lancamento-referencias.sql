-- Lancamentos que ja usam alguma referencia do arquivo (Ref1 = Reference no Service Layer).
-- Agrupado porque uma referencia generica pode estar em milhares de lancamentos antigos, e o
-- sap-odbc recusa resultado truncado: para o aviso basta um numero e a contagem.
SELECT "Ref1" AS "Referencia", MAX("TransId") AS "TransId", COUNT(*) AS "Quantidade"
FROM "OJDT"
WHERE "Ref1" IN (:referencias)
GROUP BY "Ref1"
