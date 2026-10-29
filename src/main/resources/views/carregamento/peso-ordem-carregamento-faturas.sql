SELECT
    l."DocEntry" AS "DocEntry",
    l."U_docEntryFatura" AS "Fatura",
    l."U_qtdRetirada" AS "Retirada",
    i."Weight1" AS "PesoLinha",
    i."Quantity" AS "QtdFatura"
FROM "@ORD_CRG_LINHA" l
JOIN "INV1" i ON i."DocEntry" = l."U_docEntryFatura" AND i."LineNum" = l."U_baseLine"
WHERE l."U_baseType" = 13
    AND (l."DocEntry" = :p1
    OR l."DocEntry" = :p2
    OR l."DocEntry" = :p3
    OR l."DocEntry" = :p4
    OR l."DocEntry" = :p5
    OR l."DocEntry" = :p6
    OR l."DocEntry" = :p7
    OR l."DocEntry" = :p8
    OR l."DocEntry" = :p9
    OR l."DocEntry" = :p10
    OR l."DocEntry" = :p11
    OR l."DocEntry" = :p12
    OR l."DocEntry" = :p13
    OR l."DocEntry" = :p14
    OR l."DocEntry" = :p15
    OR l."DocEntry" = :p16
    OR l."DocEntry" = :p17
    OR l."DocEntry" = :p18
    OR l."DocEntry" = :p19
    OR l."DocEntry" = :p20)
