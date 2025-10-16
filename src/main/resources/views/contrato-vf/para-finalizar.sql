SELECT
	vf."DocEntry"
FROM
	"@AR_CONTRATO_FUTURO" vf
	LEFT JOIN ODPI boleto ON vf."DocEntry" = boleto."U_venda_futura" AND boleto."DocStatus" <> 'C' AND boleto."CANCELED" = 'N'
WHERE
	vf."U_status" = 'aberto'
	AND EXISTS(SELECT 1 FROM "ODPI" WHERE "ODPI"."U_venda_futura" = vf."DocEntry" AND "ODPI"."CANCELED" = 'N')
	AND boleto."DocEntry" IS NULL