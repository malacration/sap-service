SELECT
    inst."DocEntry" AS "DocEntry",
    inst."InstlmntID" AS "InstallmentId"
FROM
    DPI6 inst
    LEFT JOIN ODPI dpi on(dpi."DocEntry" = inst."DocEntry")
WHERE
    inst."U_pix_reference" is not null
    and inst."Status" <> 'C'
    and inst."U_pix_consultar_ate" is not null
    and inst."U_pix_consultar_ate" < :now
