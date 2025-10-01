SELECT
    LCM."Ref1",
    LCM."CreatedBy",
    LCM."RefDate",
    LCM."DueDate",
    LCM."BPLName",
    LCM."Debit",
    LCM."LineMemo",
    LCM."TransType"

FROM JDT1 LCM
WHERE
    LCM."Account"   = '1.1.2.001.00001'
    AND LCM."ShortName" = :cardCode
    AND NOT EXISTS (
        SELECT 1
        FROM RCT2 T1
        WHERE T1."DocEntry" = LCM."CreatedBy"
          AND T1."InvType"  = LCM."TransType"
    )
    AND LCM."Debit" > 0
