SELECT
    inst."DocEntry" AS "DocEntry",
    inst."InstlmntID" AS "InstallmentId"
FROM
    INV6 inst
    LEFT JOIN OINV inv on(inv."DocEntry" = inst."DocEntry")
WHERE
    inst."U_pix_reference" is not null
    and inst."Status" <> 'C'
    and (inst."U_pix_consultar_ate" is null or inst."U_pix_consultar_ate" >= :now)
    and (inst."U_pix_proxima_consulta_em" is null or inst."U_pix_proxima_consulta_em" <= :now)
