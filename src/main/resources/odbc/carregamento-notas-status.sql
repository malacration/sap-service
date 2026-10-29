-- Notas (OINV) e entregas de NF de venda futura (ODLN) geradas pela ordem de carregamento, com o
-- status da NF-e na TaxPlus ("Process"/"ProcessStatus"; 4 = Autorizado). Sem linha na "Process"
-- = ainda nao foi para a SEFAZ. "Mensagem" = ultimo historico da TaxPlus ("ProcessHist", ligado pelo
-- BatchId, que existe mesmo sem chave de acesso): o motivo da rejeicao ou do erro interno.
SELECT N."DocEntry", 13 AS "DocType", N."DocNum", N."Serial", P."KeyNfe", P."StatusId", S."Description" AS "Status",
    (SELECT H."Hist" FROM "ProcessHist" H WHERE H."Id" = (SELECT MAX(X."Id") FROM "ProcessHist" X WHERE X."BatchId" = P."BatchId")) AS "Mensagem"
FROM "OINV" N
LEFT JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = 13
LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
WHERE N."U_faturadoOrdemCarregamento" = :ordem
UNION ALL
SELECT N."DocEntry", 15 AS "DocType", N."DocNum", N."Serial", P."KeyNfe", P."StatusId", S."Description" AS "Status",
    (SELECT H."Hist" FROM "ProcessHist" H WHERE H."Id" = (SELECT MAX(X."Id") FROM "ProcessHist" X WHERE X."BatchId" = P."BatchId")) AS "Mensagem"
FROM "ODLN" N
LEFT JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = 15
LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
WHERE N."U_faturadoOrdemCarregamento" = :ordem
