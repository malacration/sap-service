SELECT DISTINCT
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
FROM
	JDT1 LCM
	INNER JOIN OACT C ON LCM."Account" = C."AcctCode"
	LEFT JOIN ITR1 reconLinha ON reconLinha."TransId" = LCM."TransId"
	LEFT JOIN OITR recon ON recon."ReconNum" = reconLinha."ReconNum" AND recon."Canceled" = 'N'
WHERE
    C."LocManTran" = 'Y'
    AND LCM."ShortName" = :cardCode
    AND (LCM."TransCode" <>  'VFET' OR LCM."TransCode" IS NULL )
    AND recon."ReconNum" IS NULL