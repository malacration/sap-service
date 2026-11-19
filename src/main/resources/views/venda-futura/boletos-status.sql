SELECT
	"ODPI"."DocEntry",
	"ODPI"."DocNum",
	"ODPI"."DocDueDate",
	"ODPI"."DocTotal",
	MAX("ORIN"."DocEntry") as "Devolucao",
	MAX("ORCT"."DocEntry") as "Recebimento",
	"ODPI"."DocStatus"
FROM
	"ODPI"
	LEFT JOIN "RIN1" ON ("RIN1"."BaseEntry" = "ODPI"."DocEntry" AND "RIN1"."BaseType" = 203)
	LEFT JOIN "ORIN" ON ("ORIN"."DocEntry" = "RIN1"."DocEntry" AND "ORIN"."CANCELED" = 'N')
	LEFT JOIN "RCT2" ON ("RCT2"."DocEntry" = "ODPI"."DocEntry" AND "RCT2"."InvType" = '203')
	LEFT JOIN "ORCT" ON ("ORCT"."DocEntry" = "RCT2"."DocNum" AND "ORCT"."Canceled" = 'N')
WHERE
	"ODPI"."U_venda_futura" = :idVendaFutura
GROUP BY "ODPI"."DocEntry", "ODPI"."DocNum", "ODPI"."DocDueDate", "ODPI"."DocTotal", "ODPI"."DocStatus"
ORDER BY "ODPI"."DocDueDate"
