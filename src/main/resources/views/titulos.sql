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
    NS."DocTotal",
    P."PaidToDate",
    P."U_StatusCobranca",
    P."U_AgenteCobrador"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    INNER JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
WHERE
    NS."DocStatus" in ('O','D')
    AND NS."BPLId" in (2,4,11,17,18,12)
    AND P."InsTotal" <> '0'
    AND P."DueDate" <= :data
    AND P."Status" = 'O'
    AND V."SlpCode" = :slpCode
 ORDER BY
      P."DueDate"