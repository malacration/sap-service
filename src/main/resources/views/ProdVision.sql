SELECT
I."ItemCode",
I."ItemName",
LP."Quantity" AS "Quantidade Pedida" ,
D."MinStock" AS "Estoque minimo",
D."OnHand" AS "Estoque fabrica"
FROM OITM I
LEFT JOIN RDR1 LP ON I."ItemCode" = LP."ItemCode"
LEFT JOIN ORDR P ON LP."DocEntry" = P."DocEntry"
LEFT JOIN OITW D ON I."ItemCode" = D."ItemCode" AND D."WhsCode" = LP."WhsCode"
WHERE LP."ItemCode" LIKE 'PAC%'
AND P."DocStatus" = 'O'
and P."DocDate" >= :data
and P."DocDate" <= :data1
AND D."WhsCode" = '500.01'
AND LP."Usage" = 9



