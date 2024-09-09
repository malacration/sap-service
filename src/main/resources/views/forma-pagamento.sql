SELECT
	forma."PayMethCod",
	forma."Descript",
	forma."Branch",
	forma."DflAccount",
	banco."GLAccount",
	contaContabil."BPLId"
FROM
	OPYM as forma
	LEFT JOIN DSC1 as banco on(forma."DflAccount" = banco."Account")
	LEFT JOIN OACT contaContabil on(contaContabil."AcctCode" = banco."GLAccount")
WHERE
	(contaContabil."BPLId" = :idBranch OR contaContabil."BPLId" IS null) AND forma."U_envia_forca" = '1'
