
SELECT
	inst."DocEntry",
	inv."DocNum",
	inst."InstlmntID",
	inst."U_QrCodePix",
	inst."ObjType",
	inst."Status",
	inst."DueDate",
	inst."InsTotal",
	inst."U_pix_textContent",
	inst."U_pix_link",
	inst."U_pix_reference"
FROM
	INV6 inst
	LEFT JOIN OINV inv on(inv."DocEntry" = inst."DocEntry")
WHERE
	inv."PeyMethod" in(SELECT p."PayMethCod" FROM OPYM p WHERE p."U_gerar_pix" = '1')
    AND inst."U_pix_textContent" is null
    and inst."Status" <> 'C'
    AND inv."DocDate" >= '2023-07-10'
    AND inst."DueDate" > :now