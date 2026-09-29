SELECT h."ReconNum", h."ReconType", h."ReconDate", l."TransId", l."TransRowId",
       l."SrcObjTyp", l."SrcObjAbs", l."ReconSum"
FROM "OITR" h JOIN "ITR1" l ON l."ReconNum" = h."ReconNum"
WHERE h."Canceled" = 'N' AND h."ReconType" <> 7
    AND EXISTS (SELECT 1 FROM "ITR1" alvo WHERE alvo."ReconNum" = h."ReconNum" AND alvo."TransId" IN (:ids))
ORDER BY h."ReconNum", l."TransId", l."TransRowId"
