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
    C."U_Status" AS "U_StatusCobranca",
    C."U_Cobrador" AS "U_AgenteCobrador"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    INNER JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = NS."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    NS."DocStatus" in ('O','D')
    AND NS."BPLId" in (2,4,11,17,18,12)
    AND P."InsTotal" <> '0'
    AND P."DueDate" <= :data
    AND P."Status" = 'O'
    AND V."SlpCode" = :slpCode
 ORDER BY
      P."DueDate"