-- Parcelas em aberto das NF (OINV) do lote, com os dados da filial, para o "Comunicado de Cessao
-- de Titulo". So le. Parcela em aberto = "InsTotal" - "PaidToDate" > 0.005 (mesmo limiar da importacao);
-- so NF aberta (DocStatus O) ou entregue (D), a fechada (C) fica de fora; NF cancelada (Y) e o documento de cancelamento (C) ficam de fora. Uma linha por parcela; Saldo = InsTotal - PaidToDate.
SELECT
    N."DocEntry" AS "DocEntry",
    N."Serial" AS "Serial",
    P."InstlmntID" AS "Parcela",
    P."InsTotal" - P."PaidToDate" AS "Saldo",
    TO_VARCHAR(P."DueDate", 'YYYY-MM-DD') AS "DueDate",
    N."CardCode" AS "CardCode",
    N."CardName" AS "CardName",
    N."BPLId" AS "BPLId",
    B."BPLName" AS "BPLName",
    B."TaxIdNum" AS "TaxIdNum",
    B."Street" AS "Street",
    B."StreetNo" AS "StreetNo",
    B."Block" AS "Block",
    B."City" AS "City",
    B."State" AS "State",
    B."ZipCode" AS "ZipCode"
FROM OINV N
INNER JOIN INV6 P ON P."DocEntry" = N."DocEntry"
LEFT JOIN OBPL B ON B."BPLId" = N."BPLId"
WHERE N."DocEntry" IN (:docEntries)
  AND N."CANCELED" = 'N'
  AND N."DocStatus" IN ('O','D')
  AND P."InsTotal" - P."PaidToDate" > 0.005
ORDER BY N."DocEntry", P."InstlmntID"
