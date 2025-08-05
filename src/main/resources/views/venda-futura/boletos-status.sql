SELECT
	"ODPI"."DocNum",
	"ODPI"."DocDueDate",
	"ODPI"."DocTotal",
	"RIN1"."DocEntry" as "Devolucao",
	"ODPI"."DocStatus"
FROM
	"ODPI"
	INNER JOIN "DPI1" on("ODPI"."DocEntry" = "DPI1"."DocEntry")
	LEFT JOIN "RIN1" ON ("RIN1"."BaseEntry" = "DPI1"."DocEntry" AND "RIN1"."BaseLine" = "DPI1"."LineNum" AND "RIN1"."BaseType" = 203)
	LEFT JOIN "ORIN" ON ("ORIN"."DocEntry" = "RIN1"."DocEntry" AND "ORIN".CANCELED = 'N')
WHERE
	"ODPI"."U_venda_futura" = :idVendaFutura
ORDER BY "ODPI"."DocDueDate"