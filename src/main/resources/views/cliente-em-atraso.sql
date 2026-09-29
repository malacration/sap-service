SELECT
    tl."TransId",
    tl."ShortName",
    tl."DueDate"
FROM
    JDT1 tl
WHERE
    tl."ShortName" = :cardCode
    AND tl."DueDate" <= :dataLimite
    AND tl."BalDueDeb" > 0
