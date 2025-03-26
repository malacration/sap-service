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
where
    inst."U_pix_textContent" is not null
    and inst."Status" <> 'C'