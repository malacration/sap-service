SELECT
"T0"."DocNum",
"T0"."CardCode",
"T0"."CardName",
"T1"."ItemCode",
"T1"."Dscription",
"T1"."Quantity",
"T1"."UomCode",
b."DflWhs",
"T1"."U_ORD_CARREGAMENTO2"
FROM RDR1 "T1"
JOIN ORDR "T0" ON "T0"."DocEntry" = "T1"."DocEntry"
JOIN "OBPL" b ON "T0"."BPLId" = b."BPLId"
WHERE "T1"."U_ORD_CARREGAMENTO2" = :U_ORD_CARREGAMENTO2