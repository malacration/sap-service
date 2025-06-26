SELECT
    "OITM"."ItemCode",
    "OITM"."ItemName",
    "OBTQ"."WhsCode",
    "OBTN"."DistNumber",
    "OBTN"."ExpDate",
    "OBTN"."MnfDate",
    "OBTN"."InDate",
    "OBTQ"."Quantity"
FROM
    "OITM"
    INNER JOIN "OBTQ" ON ("OBTQ"."ItemCode" = "OITM"."ItemCode")
    INNER JOIN "OBTN" ON (
        "OBTN"."SysNumber" = "OBTQ"."SysNumber" AND
        "OITM"."ItemCode" = "OBTN"."ItemCode"
    )
WHERE
    ("OBTQ"."Quantity" > :exibeLoteVazio)
    AND "OITM"."ItemCode" = :itemCode
    AND "OBTQ"."WhsCode" = :deposito
