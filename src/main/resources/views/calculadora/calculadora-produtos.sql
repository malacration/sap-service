SELECT
	"OITM"."ItemCode",
	"OITM"."ItemName",
    "OITM"."U_linha_sustennutri",
    "OITM"."U_grupo_sustennutri"
FROM
	"OITM"
	INNER JOIN "OITT" ON "OITM"."ItemCode" =  "OITT"."Code"
WHERE
	"OITM"."ItemCode" like :search
	AND "OITM"."validFor" = 'Y'