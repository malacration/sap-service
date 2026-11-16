
SELECT
	"ODPI"."DocEntry",
	"ODPI"."DocNum",
	"ODPI"."DocDueDate",
	"ODPI"."DocTotal",
	(SELECT MAX("ORIN"."DocEntry") FROM "RIN1" INNER JOIN "ORIN" ON "ORIN"."DocEntry" = "RIN1"."DocEntry"
		WHERE "ORIN"."CANCELED" = 'N' AND "RIN1"."BaseType" = 203 AND "RIN1"."BaseEntry" = "ODPI"."DocEntry") as "Devolucao",
	(SELECT MAX("ORCT"."DocEntry") FROM "RCT2" INNER JOIN "ORCT" ON "ORCT"."DocEntry" = "RCT2"."DocNum"
		WHERE "ORCT"."Canceled" = 'N' AND "RCT2"."InvType" = '203' AND "RCT2"."DocEntry" = "ODPI"."DocEntry") as "Recebimento",
	"ODPI"."DocStatus"
FROM
	"ODPI"
WHERE
	"ODPI"."U_venda_futura" = :idVendaFutura
ORDER BY "ODPI"."DocDueDate"
