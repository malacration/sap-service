SELECT
    "OBPL"."BPLId",
    "OBPL"."BPLName",
    "OBPL"."PrefState"
FROM
    OSLP o
    INNER JOIN "@RO_FILIAL_LINHA" rfv ON o."U_filial" = rfv."DocEntry"
    INNER JOIN "OBPL" ON "OBPL"."BPLId"  = rfv."U_filial"
WHERE
    o."SlpCode" = :vendedor