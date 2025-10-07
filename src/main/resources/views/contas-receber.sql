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
    LCM."TransType"
FROM JDT1 LCM
INNER JOIN OACT C ON LCM."Account" = C."AcctCode"
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