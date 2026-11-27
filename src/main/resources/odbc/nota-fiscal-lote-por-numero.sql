-- NF-e de saida (mesmo criterio de elegibilidade da tela: OINV Model 39, nao e o documento de
-- cancelamento 'C' e tem chave de acesso na "Process") pelo numero da nota (Serial), com as parcelas.
-- Sem filtro da tela: o servico cruza com a listagem para aplicar os filtros. O numero da NF e por
-- serie/filial, entao o mesmo numero pode voltar em mais de uma nota.
SELECT
    N."DocEntry" AS "DocEntry",
    N."Serial" AS "Numero",
    N."SeriesStr" AS "Serie",
    N."BPLId" AS "BPLId",
    N."BPLName" AS "BPLName",
    N."CardName" AS "CardName",
    PA."InstlmntID" AS "Parcela",
    PA."InsTotal" AS "ValorParcela",
    PA."PaidToDate" AS "PagoParcela",
    TO_VARCHAR(PA."DueDate", 'YYYY-MM-DD') AS "VencimentoParcela"
FROM OINV N
INNER JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = N."ObjType"
LEFT JOIN INV6 PA ON PA."DocEntry" = N."DocEntry"
WHERE N."Model" = 39
  AND N."CANCELED" <> 'C'
  AND IFNULL(P."KeyNfe", '') <> ''
  AND N."Serial" IN (:numeros)
