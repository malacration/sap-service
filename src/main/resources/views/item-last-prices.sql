SELECT
    iten."ItemCode",
    iten."LastPurPrc",
    iten."LstEvlPric",
    dep."AvgPrice",
    iten."U_productionCost",
    iten."InvntryUom",
    ugp."UgpCode",
    ugp."UgpName"
FROM "OITM" iten
LEFT JOIN "OITW" dep on(dep."ItemCode" = iten."ItemCode")
LEFT JOIN "OUGP" ugp on(ugp."UgpEntry" = iten."UgpEntry")
WHERE
	iten."ItemCode" = :item
	AND dep."WhsCode" = :deposito