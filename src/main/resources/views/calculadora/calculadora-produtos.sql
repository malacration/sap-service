SELECT
	"OITM"."ItemCode",
	"OITM"."ItemName"
FROM
	"OITM"
	INNER JOIN "OITT" ON "OITM"."ItemCode" =  "OITT"."Code"
WHERE
	"OITM"."ItemCode" like :search
	AND "OITM"."validFor" = 'Y'