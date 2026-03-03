SELECT
    LCM."TransId",
    LCM."Ref1",
    LCM."CreatedBy",
    LCM."RefDate",
    LCM."DueDate",
    LCM."ShortName",
    LCM."BPLName",
    LCM."Debit",
    LCM."Credit",
    LCM."LineMemo",
    LCM."TransType",
    LCM."SourceLine",
    PARCELAS."U_QrCodePix",
    PARCELAS."U_pix_textContent",
    PARCELAS."U_pix_link",
    PARCELAS."U_pix_reference",
    PARCELAS."U_pix_due_date"
FROM JDT1 LCM
INNER JOIN OACT C ON LCM."Account" = C."AcctCode"
LEFT JOIN OINV NOTA ON NOTA."DocEntry" = LCM."CreatedBy" AND LCM."TransType" = 13
LEFT JOIN INV6 PARCELAS ON NOTA."DocEntry" = PARCELAS."DocEntry" AND LCM."SourceLine" = PARCELAS."InstlmntID"
WHERE
    C."LocManTran" = 'Y'
    AND LCM."ShortName" = :cardCode
    AND (LCM."TransCode" <> 'VFET' OR LCM."TransCode" IS NULL)
    AND LCM."InitRef3Ln" IS NOT NULL
    AND NOT EXISTS (
        SELECT 1
        FROM ITR1 I
        INNER JOIN OITR O
            ON O."ReconNum" = I."ReconNum"
           AND O."Canceled" = 'N'
        WHERE
            I."TransId" = LCM."TransId"
            AND LCM."InitRef3Ln" = I."InstID"
    )
ORDER BY LCM."RefDate" ASC