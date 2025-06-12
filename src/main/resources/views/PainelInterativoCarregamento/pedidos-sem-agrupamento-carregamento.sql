SELECT
P."DocEntry",
P."DocDate",
P."CardCode",
P."CardName",
V."SlpCode",
V."SlpName",
L."ItemCode",
L."Dscription" AS "Description",
L."Usage",
L."DistribSum",
L."Quantity",
D."OnHand"
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
        OR L."Dscription" like :search

    )
AND (
        V."SlpCode" like :salesPerson
        OR V."SlpName" like :salesPerson
)
AND (
        P."CardCode" like :partner
       OR  P."CardName" like :partner
)
ORDER BY P."DocDate" desc
