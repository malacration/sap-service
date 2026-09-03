SELECT
	"@AR_CONTRATO_FUTURO"."DocEntry",
	"OSLP"."SlpName" as "SalesEmployeeName",
	"ORDR"."DocNum" as "OrderDocNum",
	"OBPL"."BPLName" as "Bplname"
FROM
	"@AR_CONTRATO_FUTURO"
	LEFT JOIN "OSLP" ON ("@AR_CONTRATO_FUTURO"."U_vendedor" = "OSLP"."SlpCode")
	LEFT JOIN "OBPL" ON ("@AR_CONTRATO_FUTURO"."U_filial" = "OBPL"."BPLId")
	LEFT JOIN "ORDR" ON ("ORDR"."DocEntry" = "@AR_CONTRATO_FUTURO"."U_orderDocEntry")
WHERE
	"@AR_CONTRATO_FUTURO"."DocEntry" = :idContrato
