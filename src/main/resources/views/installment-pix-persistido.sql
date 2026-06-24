SELECT
    inst."InstlmntID" AS "InstallmentId",
    inst."U_QrCodePix",
    inst."U_pix_reference",
    inst."U_pix_textContent",
    inst."U_pix_link",
    inst."U_pix_due_date"
FROM
    INV6 inst
WHERE
    inst."DocEntry" = :docEntry
