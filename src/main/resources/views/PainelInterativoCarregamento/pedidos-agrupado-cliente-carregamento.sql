SELECT
P."CardCode",
P."CardName",
L."ItemCode",
L."Dscription" AS "Description",
SUM(L."Quantity") AS "Quantity",
SUM(D."OnHand") AS "OnHand"
FROM ORDR P
INNER JOIN RDR1 L ON P."DocEntry" = L."DocEntry"
left JOIN OSLP V ON V."SlpCode" = P."SlpCode"
LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
WHERE P."DocStatus" = 'O'
AND L."Usage" = 9
AND P."DocDate" >= :startDate
AND P."DocDate" <= :finalDate
AND P."BPLId" = :branch
AND  (
        L."ItemCode" like :search
    )
AND (
        V."SlpCode" like :salesPerson
)
AND (
        P."CardCode" like :partner
)
GROUP BY
P."CardCode",
P."CardName",
L."ItemCode",
L."Dscription"
ORDER BY
P."CardName",
L."ItemCode"
