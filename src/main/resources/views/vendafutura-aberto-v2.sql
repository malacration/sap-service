SELECT DISTINCT
	"ORDR"."DocEntry",
	"ORDR"."BPLId"
FROM
	"ORDR"
	INNER JOIN "RDR1" on("ORDR"."DocEntry" = "RDR1"."DocEntry")
WHERE
	"RDR1"."Usage" = :utilizacao
	AND "ORDR"."DocDate" >= :startDate
	AND "ORDR"."BPLId" = :filiais
	AND "ORDR"."U_pedido_update" = '0'
	AND ("ORDR"."U_legado_vf" IS NULL OR "ORDR"."U_legado_vf" <> '1')
	AND "ORDR"."DocStatus" = 'O'
	AND NOT EXISTS (
		SELECT 1
		FROM "INV1" nl
		INNER JOIN "OINV" n ON n."DocEntry" = nl."DocEntry"
		WHERE nl."BaseType" = 17
			AND nl."BaseEntry" = "ORDR"."DocEntry"
			AND n."CANCELED" = 'N'
	)
	AND NOT EXISTS (
		SELECT 1
		FROM "DLN1" el
		INNER JOIN "ODLN" e ON e."DocEntry" = el."DocEntry"
		WHERE el."BaseType" = 17
			AND el."BaseEntry" = "ORDR"."DocEntry"
			AND e."CANCELED" = 'N'
	)
