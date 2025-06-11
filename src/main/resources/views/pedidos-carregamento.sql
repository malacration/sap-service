SELECT
P."DocEntry",
P."DocDate",
P."CardCode",
P."CardName",
V."SlpCode",
V."SlpName",
L."ItemCode",
L."Dscription",
L."Usage",
L."DistribSum",
L."Quantity"
FROM ORDR P
INNER JOIN RDR1 L ON P."DocEntry" = L."DocEntry"
left JOIN OSLP V ON V."SlpCode" = P."SlpCode"
WHERE P."DocStatus" = 'O'
AND L."Usage" = 9
AND P."BPLId" in (2,4,11,17,18)
AND P."DocDate" >= :startDate
AND P."DocDate" <= :finalDate
AND  (
      L."ItemCode" like :search
    )
AND (
        V."SlpCode" like :salesPerson
)
AND (
        P."CardCode" like :partner
)
ORDER BY P."DocDate" desc
