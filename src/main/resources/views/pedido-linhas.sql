SELECT
    d."ItemCode",
    d."Dscription",
    d."Quantity",
    d."U_preco_negociado" AS "PrecoNegociado"
FROM "RDR1" d
WHERE d."DocEntry" = :docEntry
