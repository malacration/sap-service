-- Notas fiscais de entrada (OPCH) nao canceladas pelos DocEntry :docEntries (importacao no Nota Agil).
-- Mesmas colunas de contas-pagar-notas-entrada-periodo.sql; a filial e conferida no sap-rovema.
SELECT
    N."DocEntry" AS "DocEntry",
    N."DocNum" AS "DocNum",
    N."Serial" AS "Serial",
    N."SeriesStr" AS "SeriesStr",
    N."Model" AS "Model",
    N."U_ChaveAcesso" AS "U_ChaveAcesso",
    TO_VARCHAR(N."TaxDate", 'YYYY-MM-DD') AS "TaxDate",
    TO_VARCHAR(N."DocDate", 'YYYY-MM-DD') AS "DocDate",
    N."BPLId" AS "BPLId",
    N."BPLName" AS "BPLName",
    N."CardCode" AS "CardCode",
    N."CardName" AS "CardName",
    T."TaxId0" AS "CnpjFornecedor",
    T."TaxId4" AS "CpfFornecedor",
    N."DocTotal" AS "DocTotal",
    N."DocCur" AS "DocCur",
    E."MainCurncy" AS "MainCurncy",
    N."AtcEntry" AS "AtcEntry",
    (SELECT SUM(W."WTAmnt") FROM PCH5 W WHERE W."AbsEntry" = N."DocEntry" AND W."Category" = 'P') AS "RetencaoPagamento",
    (SELECT SUM(W."WTAmnt") FROM PCH5 W WHERE W."AbsEntry" = N."DocEntry" AND W."Category" = 'I') AS "RetencaoFatura",
    (SELECT COUNT(*) FROM ATC1 A WHERE A."AbsEntry" = N."AtcEntry" AND LOWER(A."FileExt") = 'pdf') AS "AnexosPdf"
FROM OPCH N
LEFT JOIN PCH12 T ON T."DocEntry" = N."DocEntry"
CROSS JOIN OADM E
WHERE N."CANCELED" = 'N'
  AND N."DocEntry" IN (:docEntries)
ORDER BY N."DocEntry"
