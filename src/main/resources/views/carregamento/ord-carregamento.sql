SELECT
    "T0"."DocEntry",
    "T0"."DocNum",
    "T0"."CardCode",
    "T0"."CardName",
    "T1"."ItemCode",
    "T1"."Dscription",
    "T1"."Quantity",
    "T1"."UomCode",
    b."DflWhs",
    "T1"."U_ORD_CARREGAMENTO",
    "T0"."Address2",
    "T0"."Comments",
    "T1"."Weight1"
FROM RDR1 "T1"
    JOIN ORDR "T0" ON "T0"."DocEntry" = "T1"."DocEntry"
    JOIN "OBPL" b ON "T0"."BPLId" = b."BPLId"
    WHERE "T1"."U_ORD_CARREGAMENTO" = :U_ORD_CARREGAMENTO