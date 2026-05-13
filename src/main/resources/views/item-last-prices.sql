SELECT
    iten."ItemCode",
    iten."LastPurPrc",
    iten."LstEvlPric",
    dep."AvgPrice",
    iten."U_productionCost"
FROM "OITM" iten
LEFT JOIN "OITW" dep on(dep."ItemCode" = iten."ItemCode")
WHERE
	iten."ItemCode" = :item
	AND dep."WhsCode" = :deposito