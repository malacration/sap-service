SELECT
	"@AR_CONTRATO_FUTURO"."DocEntry",
	"@AR_CONTRATO_FUTURO"."U_orderDocEntry",
	"@AR_CONTRATO_FUTURO"."U_cardCode",
	"@AR_CONTRATO_FUTURO"."U_vendedor",
	"@AR_CONTRATO_FUTURO"."U_cardName",
	"@AR_CONTRATO_FUTURO"."U_filial",
	"@AR_CONTRATO_FUTURO"."U_valorFrete",
	"@AR_CONTRATO_FUTURO"."U_status",
	"@AR_CONTRATO_FUTURO"."DocNum",
	"@AR_CONTRATO_FUTURO"."Series",
	"@AR_CONTRATO_FUTURO"."U_dataCriacao",
	"OSLP"."SlpName" as "SalesEmployeeName",
	"ORDR"."DocNum" as "OrderDocNum",
	"OBPL"."BPLName" as "Bplname",
    SUM("@AR_CF_LINHA"."U_quantity") AS "TotalProdutosCalculado"
FROM
	"@AR_CONTRATO_FUTURO"
	INNER JOIN "@AR_CF_LINHA" ON ("@AR_CF_LINHA"."DocEntry" = "@AR_CONTRATO_FUTURO"."DocEntry")
	INNER JOIN "OSLP" ON ("@AR_CONTRATO_FUTURO"."U_vendedor" = "OSLP"."SlpCode")
	INNER JOIN "OBPL" ON ("@AR_CONTRATO_FUTURO"."U_filial" = "OBPL"."BPLId")
	INNER JOIN "ORDR" ON ("ORDR"."DocEntry" = "@AR_CONTRATO_FUTURO"."U_orderDocEntry" )
where
    ("@AR_CONTRATO_FUTURO"."U_vendedor" = :vendedor or "@AR_CONTRATO_FUTURO"."U_vendedor" < :superVendedor)
group by
	"@AR_CONTRATO_FUTURO"."DocEntry",
	"@AR_CONTRATO_FUTURO"."U_orderDocEntry",
	"@AR_CONTRATO_FUTURO"."U_cardCode",
	"@AR_CONTRATO_FUTURO"."U_vendedor",
	"@AR_CONTRATO_FUTURO"."U_cardName",
	"@AR_CONTRATO_FUTURO"."U_filial",
	"@AR_CONTRATO_FUTURO"."U_valorFrete",
	"@AR_CONTRATO_FUTURO"."U_status",
	"@AR_CONTRATO_FUTURO"."DocNum",
	"@AR_CONTRATO_FUTURO"."Series",
	"@AR_CONTRATO_FUTURO"."U_dataCriacao",
	"OSLP"."SlpName",
	"ORDR"."DocNum",
	"OBPL"."BPLName"
ORDER BY "@AR_CONTRATO_FUTURO"."U_dataCriacao" DESC