SELECT
    NS."BPLName",
    NS."CardCode",
    NS."CardName",
    V."SlpCode",
    V."SlpName",
    V."Email" as SlpEmail,
    NS."Serial",
    P."InstlmntID",
    P."InsTotal",
    P."DueDate",
    NS."DocTotal"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    INNER JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
    LEFT  JOIN RCT2 CR ON CR."DocEntry" = NS."DocEntry" AND CR."DocTransId" = NS."TransId" AND CR."InstId" = P."InstlmntID"
    LEFT  JOIN ORCT OCR ON CR."DocEntry" = OCR."DocEntry"
WHERE
    NS."DocStatus" = 'O'
    AND NS."BPLId" in (2,4,11,17)
    AND P."InsTotal" <> '0'
    AND P."DueDate" <= :data
    AND (CR."DocNum" IS NULL AND (OCR."Canceled" = 'N' OR OCR."Canceled" IS NULL))
    AND V."SlpCode" = :slpCode