SELECT DISTINCT
    dpi."DocEntry"
FROM
    DPI6 inst
    LEFT JOIN ODPI dpi on(dpi."DocEntry" = inst."DocEntry")
where
    inst."U_pix_reference" = :reference
