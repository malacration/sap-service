SELECT
P."CardCode",
P."CardName",
L."ItemCode",
L."Dscription" AS "Description",
SUM(L."Quantity") AS "Quantity",
SUM(D."OnHand") AS "OnHand",
ocl."U_orderDocEntry" AS "EmOrdemDeCarregamento"
FROM ORDR P
INNER JOIN RDR1 L ON P."DocEntry" = L."DocEntry"
left JOIN OSLP V ON V."SlpCode" = P."SlpCode"
LEFT JOIN OITW D ON D."WhsCode" = L."WhsCode" AND D."ItemCode" = L."ItemCode"
LEFT JOIN "@ORD_CRG_LINHA" ocl  ON L."DocEntry" = ocl."U_orderDocEntry" AND L."ItemCode" = ocl."U_itemCode"
LEFT JOIN "@ORD_CARREGAMENTO" oc ON ocl."DocEntry" = oc."DocEntry" AND oc."U_Status" = 'Aberto'
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
L."Dscription",
ocl."U_orderDocEntry"
ORDER BY
P."CardName",
L."ItemCode"
