SELECT DISTINCT
	"ORDR"."DocEntry"
FROM
	"ORDR"
	LEFT JOIN "RDR1" on("ORDR"."DocEntry" = "RDR1"."DocEntry")
WHERE
	"RDR1"."Usage" = :utilizacao
	AND "ORDR"."DocDate" = :startDate
	AND "ORDR"."U_pedido_update" = '0'
	AND "ORDR"."DocStatus" = 'O'