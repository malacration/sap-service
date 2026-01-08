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
    "T1"."Weight1",
    d."Name"
FROM RDR1 "T1"
    JOIN ORDR "T0" ON "T0"."DocEntry" = "T1"."DocEntry"
    JOIN "OBPL" b ON "T0"."BPLId" = b."BPLId"
    LEFT JOIN "CRD1" c ON c."CardCode" = "T0"."CardCode"
    LEFT JOIN "@RO_LOCAIS" d ON d."Code" = c."U_Localidade"
    WHERE "T1"."U_ORD_CARREGAMENTO" = :U_ORD_CARREGAMENTO
    AND c."Address" = 'ENTREGA'