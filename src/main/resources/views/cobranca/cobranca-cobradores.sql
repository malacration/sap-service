SELECT DISTINCT
    C."U_Cobrador" AS "Cobrador"
FROM "@COB_TITULO" C
WHERE
    C."U_Cobrador" IS NOT NULL
    AND C."U_Cobrador" <> ''
ORDER BY C."U_Cobrador"
