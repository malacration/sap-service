SELECT
    inst."DocEntry" AS "DocEntry",
    inst."InstlmntID" AS "InstallmentId",
    dpi."DocDate",
    dpi."DocNum",
    dpi."U_TX_DocEntryRef",
    dpi."U_TX_DocTypeRef"
FROM
    DPI6 inst
        LEFT JOIN ODPI dpi on(dpi."DocEntry" = inst."DocEntry")
WHERE
    (
        inst."U_pix_reference" is not NULL
        and inst."U_pix_consultar_ate" is not null
        and inst."U_pix_consultar_ate" < :now
        OR (
        dpi."U_TX_DocEntryRef" IS NOT NULL AND dpi."U_TX_DocTypeRef" IS NOT NULL
        and inst."U_pix_consultar_ate" IS NULL
        )
    )
  and dpi."DocStatus" <> 'C'
  and dpi."U_venda_futura" IS NULL
