SELECT
    tl."TransId",
    tl."ShortName",
    tl."DueDate"
FROM
    OJDT t
    LEFT JOIN JDT1 tl ON t."TransId" = tl."TransId"
    LEFT JOIN ITR1 rl ON rl."TransId" = t."TransId"
    LEFT JOIN OITR r ON r."ReconNum" = rl."ReconNum"
WHERE
    tl."ShortName" = :cardCode
    AND tl."DueDate" <= :dataLimite
    AND r."ReconNum" IS NULL
